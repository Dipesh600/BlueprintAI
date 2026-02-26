/**
 * Prompt Templates for BlueprintAI
 *
 * Each function returns a { system, user } pair that is fed
 * sequentially into MiniMax AI to build the full blueprint.
 */

export interface PromptPair {
    system: string;
    user: string;
}

/**
 * Step 1: Market Research
 */
export function marketResearchPrompt(
    ideaTitle: string,
    ideaDescription: string,
    features: string[]
): PromptPair {
    return {
        system: `You are a senior product strategist and market research analyst. 
Your task is to produce a clear, structured market research report for a mobile/web app idea.
Include the following sections:
1. Executive Summary
2. Target Market & User Personas (at least 3 personas)
3. Competitive Landscape (5 competitors with strengths/weaknesses)
4. Market Size & Opportunity (TAM, SAM, SOM estimates)
5. SWOT Analysis
6. Key Differentiators & Value Proposition
7. Go-to-Market Strategy Recommendations

Format your output in clean Markdown. Be specific and data-driven.`,
        user: `App Idea: ${ideaTitle}

Description: ${ideaDescription}

Proposed Features:
${features.map((f, i) => `${i + 1}. ${f}`).join("\n")}

Please produce a comprehensive market research report for this app idea.`,
    };
}

/**
 * Step 2: PRD (Product Requirements Document)
 */
export function prdPrompt(
    ideaTitle: string,
    ideaDescription: string,
    features: string[],
    marketResearch: string
): PromptPair {
    return {
        system: `You are a senior product manager writing a professional Product Requirements Document (PRD).
Use the market research provided as context. Your PRD must include:
1. Product Overview & Vision
2. Goals & Success Metrics (KPIs)
3. User Stories (at least 10, in "As a [user], I want to [action] so that [benefit]" format)
4. Functional Requirements (detailed, grouped by feature area)
5. Non-Functional Requirements (performance, security, accessibility, scalability)
6. Data Requirements & Models
7. API Specifications (key endpoints)
8. Release Plan & Milestones (MVP, v1.0, v2.0)
9. Assumptions & Constraints
10. Risks & Mitigations

Format in clean Markdown with tables where appropriate. Be thorough and specific.`,
        user: `App: ${ideaTitle}
Description: ${ideaDescription}
Features: ${features.join(", ")}

Market Research Context:
${marketResearch}

Generate a complete PRD.`,
    };
}

/**
 * Step 3: Wireframe Descriptions
 */
export function wireframePrompt(
    ideaTitle: string,
    features: string[],
    prd: string
): PromptPair {
    return {
        system: `You are a senior UX designer. Based on the PRD provided, create detailed wireframe descriptions for every screen in the app.

For EACH screen include:
1. Screen Name
2. Purpose
3. Layout Description (header, body, footer sections)
4. UI Components (buttons, inputs, cards, lists, modals etc. with exact labels)
5. Navigation (where each interactive element leads)
6. States (empty, loading, error, success)
7. An ASCII art wireframe sketch showing the approximate layout

Group screens by user flow (Onboarding, Core, Settings, etc.).
Format in clean Markdown.`,
        user: `App: ${ideaTitle}
Features: ${features.join(", ")}

PRD Context:
${prd}

Generate detailed wireframe descriptions for all screens.`,
    };
}

/**
 * Step 4: System Design Document
 */
export function systemDesignPrompt(
    ideaTitle: string,
    features: string[],
    prd: string
): PromptPair {
    return {
        system: `You are a senior software architect. Based on the PRD provided, create a comprehensive system design document.

Include:
1. Architecture Overview (monolith vs microservices, diagram description)
2. Tech Stack Recommendation (frontend, backend, database, cloud, with justifications)
3. Database Schema (tables, columns, types, relationships in detail)
4. API Design (RESTful endpoints with request/response examples)
5. Authentication & Authorization Flow
6. Data Flow Diagrams (describe key user flows)
7. Third-Party Integrations
8. Scalability Strategy (caching, CDN, load balancing, horizontal scaling)
9. Security Considerations (encryption, OWASP, rate limiting)
10. DevOps & CI/CD Pipeline
11. Monitoring & Observability
12. Cost Estimation (cloud resources)

Format in clean Markdown with code blocks for schemas and API examples.`,
        user: `App: ${ideaTitle}
Features: ${features.join(", ")}

PRD Context:
${prd}

Generate a comprehensive system design document.`,
    };
}
