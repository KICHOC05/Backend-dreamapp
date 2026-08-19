# Security policy

## Reporting

Do not disclose vulnerabilities in a public issue. Report them privately to the repository maintainers using GitHub private vulnerability reporting.

Include the affected component, reproduction steps, impact and any suggested mitigation. Do not include real health data, credentials or tokens.

## Secret handling

- Store `AI_API_KEY`, `FUNCTIONS_INTERNAL_KEY`, database credentials and signing keys only in the secret stores of Render, Firebase or GitHub Actions.
- Never commit service-account JSON files, `.env` files, APK signing keystores or production logs.
- Rotate a secret immediately if it appears in Git history; deleting the file alone is not sufficient.

## Release requirements

A release must pass backend compilation/tests, TypeScript compilation, `npm audit`, Android lint and CodeQL. Production Android builds must use HTTPS/WSS and release minification.

