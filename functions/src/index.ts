/**
 * BlueprintAI Cloud Functions — Main Entry Point
 *
 * Exports the `generatePRD` HTTPS Callable function which:
 * 1. Receives the idea from the Android app
 * 2. Runs the 4-stage prompt chain via MiniMax AI
 * 3. Stores generated Markdown content in Firestore
 * 4. Generates PDFs and uploads them to Firebase Storage
 * 5. Updates the project status to "done"
 */

import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { callMiniMaxAI } from "./services/minimax";
import {
    marketResearchPrompt,
    prdPrompt,
    wireframePrompt,
    systemDesignPrompt,
} from "./prompts/templates";
import { generatePDF } from "./services/pdfGenerator";

admin.initializeApp();

const db = admin.firestore();
const storage = admin.storage();

/**
 * generatePRD — Main callable function
 *
 * Called from the Android app via Firebase Functions SDK.
 * Runs the full AI generation pipeline.
 */
export const generatePRD = onCall(
    { timeoutSeconds: 540, memory: "1GiB" },
    async (request) => {
        // --- Auth check ---
        if (!request.auth) {
            throw new HttpsError("unauthenticated", "User must be logged in.");
        }

        const { projectId, userId, ideaTitle, ideaDescription, features } =
            request.data;

        if (!projectId || !userId || !ideaTitle || !ideaDescription) {
            throw new HttpsError(
                "invalid-argument",
                "Missing required fields: projectId, userId, ideaTitle, ideaDescription"
            );
        }

        const projectRef = db
            .collection("users")
            .doc(userId)
            .collection("projects")
            .doc(projectId);

        try {
            // --- Update status: generating ---
            await projectRef.update({ status: "processing" });

            // =============================================
            // STEP 1: Market Research
            // =============================================
            console.log(`[${projectId}] Step 1: Market Research`);
            await projectRef.update({ currentStep: "Market Research" });

            const researchPrompt = marketResearchPrompt(
                ideaTitle,
                ideaDescription,
                features || []
            );
            const marketResearch = await callMiniMaxAI(
                researchPrompt.system,
                researchPrompt.user,
                4096
            );

            await projectRef.update({ marketResearchContent: marketResearch });

            // =============================================
            // STEP 2: PRD
            // =============================================
            console.log(`[${projectId}] Step 2: PRD`);
            await projectRef.update({ currentStep: "Product Requirements Document" });

            const prdP = prdPrompt(
                ideaTitle,
                ideaDescription,
                features || [],
                marketResearch
            );
            const prdContent = await callMiniMaxAI(prdP.system, prdP.user, 8192);

            await projectRef.update({ prdContent });

            // =============================================
            // STEP 3: Wireframes
            // =============================================
            console.log(`[${projectId}] Step 3: Wireframes`);
            await projectRef.update({ currentStep: "Wireframe Descriptions" });

            const wireP = wireframePrompt(ideaTitle, features || [], prdContent);
            const wireframeContent = await callMiniMaxAI(
                wireP.system,
                wireP.user,
                8192
            );

            await projectRef.update({ wireframeContent });

            // =============================================
            // STEP 4: System Design
            // =============================================
            console.log(`[${projectId}] Step 4: System Design`);
            await projectRef.update({ currentStep: "System Design Document" });

            const sysP = systemDesignPrompt(ideaTitle, features || [], prdContent);
            const systemDesignContent = await callMiniMaxAI(
                sysP.system,
                sysP.user,
                8192
            );

            await projectRef.update({ systemDesignContent });

            // =============================================
            // STEP 5: Generate PDFs & Upload
            // =============================================
            console.log(`[${projectId}] Step 5: Generating PDFs`);
            await projectRef.update({ currentStep: "Generating PDFs" });

            const bucket = storage.bucket();
            const basePath = `users/${userId}/projects/${projectId}`;

            // Generate and upload all three PDFs in parallel
            const [prdPdf, wireframePdf, systemDesignPdf] = await Promise.all([
                generatePDF(`${ideaTitle} - PRD`, prdContent, {
                    subject: "Product Requirements Document",
                }),
                generatePDF(`${ideaTitle} - Wireframes`, wireframeContent, {
                    subject: "Wireframe Descriptions",
                }),
                generatePDF(`${ideaTitle} - System Design`, systemDesignContent, {
                    subject: "System Design Document",
                }),
            ]);

            const uploadFile = async (
                buffer: Buffer,
                fileName: string
            ): Promise<string> => {
                const file = bucket.file(`${basePath}/${fileName}`);
                await file.save(buffer, {
                    metadata: { contentType: "application/pdf" },
                });
                // Make publicly readable and return public URL
                await file.makePublic();
                return file.publicUrl();
            };

            const [prdUrl, wireframeUrl, systemDesignUrl] = await Promise.all([
                uploadFile(prdPdf, "prd.pdf"),
                uploadFile(wireframePdf, "wireframes.pdf"),
                uploadFile(systemDesignPdf, "system_design.pdf"),
            ]);

            // =============================================
            // DONE — Update project with URLs & final status
            // =============================================
            await projectRef.update({
                status: "done",
                currentStep: "Complete",
                outputs: {
                    prdUrl,
                    wireframeUrl,
                    systemDesignUrl,
                },
            });

            console.log(`[${projectId}] ✅ Generation complete`);
            return { success: true, projectId };
        } catch (error: any) {
            console.error(`[${projectId}] ❌ Generation failed:`, error);
            await projectRef.update({
                status: "failed",
                errorMessage: error.message || "Unknown error",
            });
            throw new HttpsError("internal", error.message);
        }
    }
);
