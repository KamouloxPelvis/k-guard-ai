# Security Policy

## Supported Versions

K-Guard AI is currently in active development. Security updates are provided for the following versions:

| Version | Supported |
|---------|-----------|
| latest  | ✅ |
| 0.4.x   | ✅ |
| < 0.4.0 | ❌ |

## Reporting a Vulnerability

Security issues should not be reported through public GitHub issues.

If you discover a vulnerability in K-Guard AI:

- Contact the maintainer directly through GitHub: **KamouloxPelvis**
- Or use the professional links listed in the repository profile and project documentation
- Provide a clear description of the issue, its impact, affected components, and reproducible steps
- Include logs, payload samples, or screenshots only if they are sanitized and do not expose secrets

A first acknowledgement target is **within 48 hours**, followed by an initial assessment and a remediation timeline when the issue is confirmed.

## Scope

This repository currently focuses on:

- alert intake and validation
- sanitization of sensitive values before analysis output
- deterministic incident classification and response shaping
- optional local LLM enrichment through Ollama

The local LLM path is designed for self-hosted usage and should be considered experimental until additional hardening, output validation, and guardrails are implemented.

## Security Expectations

When testing or reviewing this project:

- Do not include real secrets, production credentials, or customer data in reports
- Prefer isolated local or lab environments
- Assume that prompts, logs, and model outputs require verification before operational use
- Treat all LLM-generated content as analyst assistance, not as an authoritative security verdict

## Disclaimer & Legal Notice

### Educational and Portfolio Purpose

K-Guard AI is developed as a personal DevSecOps and security engineering project for portfolio, experimentation, and learning purposes.

### As-Is Basis

This software is provided **"as is"**, without warranty of any kind, express or implied.

### Production Use

K-Guard AI demonstrates alert triage, sanitization, and local AI-assisted enrichment patterns, but it is **not intended for unsupervised production use** without a professional security review, additional testing, and environment-specific hardening.

### Responsibility

The author, **Kamal Guidadou**, cannot be held responsible for damage, service disruption, or data loss resulting from the use, misuse, or deployment of this project.
