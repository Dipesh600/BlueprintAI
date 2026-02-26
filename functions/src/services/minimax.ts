/**
 * MiniMax AI Service
 * Handles all communication with the MiniMax AI API for generating
 * PRDs, wireframes, and system design documents.
 */

import axios from "axios";
import { defineString } from "firebase-functions/params";

// Use Firebase params for secrets — set via `firebase functions:config:set` or Secret Manager
const MINIMAX_API_KEY = defineString("MINIMAX_API_KEY");
const MINIMAX_GROUP_ID = defineString("MINIMAX_GROUP_ID");

const MINIMAX_BASE_URL = "https://api.minimax.chat/v1/text/chatcompletion_v2";

interface MiniMaxMessage {
    role: "system" | "user" | "assistant";
    content: string;
}

interface MiniMaxResponse {
    choices: Array<{
        message: {
            content: string;
        };
    }>;
}

/**
 * Calls MiniMax AI with the given system prompt and user message.
 * Returns the generated text content.
 */
export async function callMiniMaxAI(
    systemPrompt: string,
    userMessage: string,
    maxTokens: number = 4096
): Promise<string> {
    const messages: MiniMaxMessage[] = [
        { role: "system", content: systemPrompt },
        { role: "user", content: userMessage },
    ];

    try {
        const response = await axios.post<MiniMaxResponse>(
            MINIMAX_BASE_URL,
            {
                model: "abab6.5s-chat",
                messages,
                tokens_to_generate: maxTokens,
                temperature: 0.7,
                top_p: 0.9,
            },
            {
                headers: {
                    Authorization: `Bearer ${MINIMAX_API_KEY.value()}`,
                    "Content-Type": "application/json",
                },
                params: {
                    GroupId: MINIMAX_GROUP_ID.value(),
                },
            }
        );

        const content = response.data?.choices?.[0]?.message?.content;
        if (!content) {
            throw new Error("No content in MiniMax response");
        }
        return content;
    } catch (error: any) {
        console.error("MiniMax API Error:", error.response?.data || error.message);
        throw new Error(`MiniMax AI failed: ${error.message}`);
    }
}
