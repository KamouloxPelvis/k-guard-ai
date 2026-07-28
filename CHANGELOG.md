# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added
- Planned security baseline for v0.8.0 with GitHub CodeQL analysis for the Java codebase
- Planned automated dependency update management with Dependabot for Maven and GitHub Actions

### Changed
- Next release focus moved to security hardening of the application, delivery workflow, and Kubernetes assets

### Security
- Planned review and hardening of container, manifest, and dependency security defaults

## [0.7.0] - 2026-07-28

### Added
- Added an interactive Kubernetes installer in `installer/`
- Added namespace-aware deployment flow with default use of `k-guard` when present
- Added dynamic creation and update of the Kubernetes secret for Elasticsearch credentials
- Added installer commands for cluster checks, install, and status workflows

### Changed
- Updated the root README for the v0.7.0 deployment and installer workflow
- Documented a simple target-machine deployment path based on `git clone` and `git pull`
- Clarified Kubernetes-oriented deployment behavior and runtime expectations

### Fixed
- Fixed the README clone command example and aligned repository references with the current GitHub repository
