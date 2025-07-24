# Contributing to Keyple Demo Project

Thank you for your interest in contributing to the Keyple Demo ecosystem! This guide covers how to contribute effectively to our newly reorganized documentation structure and codebase.

## 📋 Table of Contents

- [Quick Start for Contributors](#quick-start-for-contributors)
- [Documentation Contributions](#documentation-contributions)
- [Code Contributions](#code-contributions)
- [Testing Guidelines](#testing-guidelines)
- [Submission Process](#submission-process)
- [Community Guidelines](#community-guidelines)

## Quick Start for Contributors

### 1. Fork and Clone
```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR-USERNAME/keyple-demo-ticketing.git
cd keyple-demo-ticketing

# Add upstream remote
git remote add upstream https://github.com/calypsonet/keyple-demo-ticketing.git
```

### 2. Set Up Development Environment
```bash
# Install documentation validation tools
npm install -g markdown-link-check markdownlint-cli2

# Make scripts executable
chmod +x scripts/validate-documentation.sh

# Validate current state
./scripts/validate-documentation.sh full-check
```

### 3. Create Feature Branch
```bash
# Sync with upstream
git fetch upstream
git checkout main
git merge upstream/main

# Create feature branch
git checkout -b feature/your-contribution-name
```

## Documentation Contributions

Our documentation follows a **standardized structure** across all components. Please familiarize yourself with the new organization:

### Documentation Structure

```
keyple-demo-ticketing/
├── README.md                    # 🏠 Main project overview
├── CONTRIBUTING.md              # 📝 This file
├── common/README.md             # 📚 Shared data structures
├── reloading-remote/            # 🔄 Remote reload system
│   ├── server/README.md         # 🖥️  Server setup and API
│   └── client/
│       ├── keyple-mobile-android/README.md    # 📱 Android client
│       ├── pc-dotnet/README.md                # 🖥️  .NET desktop client
│       └── interop-mobile-multiplatform/README.md # 🌐 Multiplatform client
├── validation/README.md         # ✅ Validation terminal
└── control/README.md            # 🔍 Control terminal
```

### README Structure Requirements

Each README must follow this structure:

```markdown
# [Component Name]
[![Badges](...)  
Brief description and ecosystem role.
[⬅️ Back to Main Project](../README.md)

## Overview
## Prerequisites  
## Installation
## Configuration
## Usage
## Technical Architecture (for apps/clients)
## Troubleshooting
## Related Documentation
## License
```

### Making Documentation Changes

#### 1. Before Making Changes
```bash
# Validate current documentation state
./scripts/validate-documentation.sh validate

# Check for broken links
./scripts/validate-documentation.sh check-links
```

#### 2. Writing Guidelines

**Content Principles**:
- **User-focused**: Write for the reader's needs, not internal knowledge
- **Consistent terminology**: Use terms defined in [Common Library](common-lib/README.md)
- **Actionable**: Provide clear steps users can follow
- **Current**: Keep examples and links up to date

**Style Guidelines**:
- Use **clear, concise English** suitable for international developers
- **Define technical terms** on first use
- **Include working examples** for configuration and code
- **Cross-reference** related documentation appropriately

#### 3. Common Documentation Tasks

**Adding New Component Documentation**:
```bash
# Generate template for new component
./scripts/validate-documentation.sh template "My Component" client my-component/README.md

# Edit generated template
vim my-component/README.md

# Validate structure
./scripts/validate-documentation.sh validate my-component/README.md client
```

**Updating Existing Documentation**:
- **Don't duplicate content** - reference Common Library for shared concepts
- **Update cross-references** when moving or renaming sections
- **Test all examples** before committing changes
- **Check impact on related files**

**Adding New Features**:
When adding features that affect multiple components:
1. **Update Common Library** first if data structures change
2. **Update affected application README files**
3. **Add troubleshooting sections** for new failure modes
4. **Update main README** if architecture changes

#### 4. Documentation Quality Checks

Before submitting documentation changes:

```bash
# Run full validation suite
./scripts/validate-documentation.sh full-check

# Check specific files
./scripts/validate-documentation.sh validate path/to/README.md component-type

# Verify links work
./scripts/validate-documentation.sh check-links

# Check consistency across files
./scripts/validate-documentation.sh consistency
```

**Manual Review Checklist**:
- [ ] New content follows established structure
- [ ] All links work and point to correct sections
- [ ] Examples are tested and functional
- [ ] Terminology matches Common Library definitions
- [ ] Cross-references are updated appropriately
- [ ] No duplicate content from other README files

## Code Contributions

### Coding Standards

Each component has specific coding standards:

- **Java (Server, Android apps)**: Follow Google Java Style Guide
- **Kotlin (KMP client)**: Follow Kotlin Coding Conventions
- **C# (.NET client)**: Follow Microsoft C# Coding Standards
- **JavaScript (Server dashboard)**: Follow Airbnb JavaScript Style Guide

### Component-Specific Guidelines

#### Server Contributions
- **Test with actual SAM hardware** when possible
- **Provide mock implementations** for testing without hardware
- **Update API documentation** for any endpoint changes
- **Consider security implications** of all changes

#### Mobile App Contributions (Android, iOS)
- **Test on multiple device types** and OS versions
- **Consider NFC antenna variations** across devices
- **Update plugin configurations** if hardware support changes
- **Test battery usage impact** for portable terminals

#### Desktop Client Contributions
- **Test cross-platform compatibility** (Windows/Mac/Linux)
- **Verify PC/SC reader support** across platforms
- **Update hardware compatibility lists**
- **Consider accessibility requirements**

### Development Workflow

#### 1. Setting Up Development Environment

**Server Development**:
```bash
cd reloading-remote/server
./gradlew build
./gradlew startServer  # Requires SAM hardware
```

**Android Development**:
```bash
cd validation  # or control
./gradlew assembleDebug
./gradlew connectedAndroidTest  # Requires connected device
```

**Desktop Client Development**:
```bash
cd reloading-remote/client/pc-dotnet
dotnet build
dotnet test
dotnet run
```

**KMP Development**:
```bash
cd reloading-remote/client/interop-mobile-multiplatform
./gradlew build

# Platform-specific builds
./gradlew :composeApp:assembleDebug                    # Android
./gradlew :composeApp:iosSimulatorArm64Test           # iOS Simulator
./gradlew :composeApp:run                             # Desktop
```

#### 2. Testing Your Changes

**Unit Tests**:
```bash
# Run component-specific tests
./gradlew test

# Run integration tests (requires hardware)
./gradlew integrationTest
```

**Documentation Tests**:
```bash
# Validate documentation changes
./scripts/validate-documentation.sh full-check

# Test installation instructions manually
# Follow your updated documentation on a clean system
```

**End-to-End Testing**:
1. **Card Personalization**: Use Reload Demo to initialize test cards
2. **Validation Flow**: Test cards with Validation Demo
3. **Control Verification**: Inspect validated cards with Control Demo
4. **Cross-Platform**: Test on different devices/OS versions

## Testing Guidelines

### Test Categories

#### 1. Unit Tests
- **Business Logic**: Core algorithms and data processing
- **Data Structures**: Parsing and serialization of card data
- **Hardware Abstraction**: Mock readers and SAM operations

#### 2. Integration Tests
- **Card Communication**: Real card operations (requires hardware)
- **Server Integration**: Client-server communication flows
- **Plugin Integration**: Hardware plugin functionality

#### 3. Documentation Tests
- **Link Validation**: All cross-references work correctly
- **Example Verification**: Code examples compile and run
- **Installation Testing**: Setup procedures work on clean systems

### Testing Hardware Requirements

**Minimal Testing Setup**:
- 1 PC/SC compatible reader
- 1 Calypso test card (any supported AID)
- 1 Storage card for comparison testing
- Network access to test server

**Full Testing Setup**:
- Multiple card reader types (NFC, PC/SC, proprietary)
- Multiple card types (different AIDs, security levels)
- SAM hardware for secure operations
- Multiple Android devices with varying NFC capabilities

### Test Data Management

**Test Cards**:
- Use only **test cards** with test keys
- **Never commit** production card data or keys
- **Document** test card configurations in test documentation
- **Reset cards** to clean state between test runs

**Test Environments**:
- **Development**: Local setup with mock data
- **Integration**: Shared server with test SAM
- **Staging**: Production-like environment with test cards only

## Submission Process

### 1. Pre-submission Checklist

Before creating a pull request:

**Code Quality**:
- [ ] All tests pass locally
- [ ] Code follows component coding standards
- [ ] No debug or temporary code committed
- [ ] Security review completed (if applicable)

**Documentation Quality**:
- [ ] Documentation validation passes
- [ ] All examples tested and working
- [ ] Cross-references updated appropriately
- [ ] No duplicate content across files

**Testing**:
- [ ] Unit tests added for new functionality
- [ ] Integration tests pass with available hardware
- [ ] Manual testing completed on target platforms
- [ ] Regression testing completed

### 2. Creating Pull Request

```bash
# Ensure branch is up to date
git fetch upstream
git rebase upstream/main

# Push to your fork
git push origin feature/your-contribution-name
```

**Pull Request Description Template**:
```markdown
## Description
Brief description of changes and motivation.

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality) 
- [ ] Breaking change (would cause existing functionality to not work)
- [ ] Documentation update
- [ ] Performance improvement

## Components Affected
- [ ] Main project documentation
- [ ] Common Library
- [ ] Reloading Remote Server
- [ ] Android Client
- [ ] .NET Client  
- [ ] KMP Client
- [ ] Validation App
- [ ] Control App

## Testing Completed
- [ ] Unit tests pass
- [ ] Integration tests pass  
- [ ] Documentation validation passes
- [ ] Manual testing on [list platforms/devices]

## Hardware Tested
- [ ] Standard NFC smartphones
- [ ] PC/SC readers: [list specific models]
- [ ] SAM integration: [describe setup]
- [ ] Proprietary terminals: [list if applicable]

## Breaking Changes
Describe any breaking changes and migration steps.

## Additional Notes
Any additional information reviewers should know.
```

### 3. Review Process

**Automated Checks**:
- GitHub Actions workflows validate documentation and run tests
- Link checking and markdown linting
- Cross-platform build verification

**Human Review**:
- **Code Review**: Technical correctness and standards compliance
- **Documentation Review**: Clarity, accuracy, and completeness
- **Architecture Review**: Consistency with project design
- **Security Review**: For changes affecting cryptographic operations

**Review Timeline**:
- **Simple fixes**: 1-2 business days
- **Feature additions**: 3-5 business days
- **Major changes**: 1-2 weeks (may require multiple review rounds)

### 4. Merging Process

**Requirements for Merge**:
- All automated checks pass
- At least one approving review from project maintainer
- No unresolved review comments
- Documentation updated appropriately

**Post-Merge**:
- Monitor CI/CD pipelines for issues
- Update any dependent documentation
- Announce significant changes to community

## Community Guidelines

### Communication Channels

- **GitHub Issues**: Bug reports, feature requests, technical discussions
- **Pull Request Comments**: Code review and implementation discussion
- **CNA Contact**: For access to proprietary plugins or formal support

### Contribution Recognition

Contributors are recognized through:
- **Git commit attribution**: Proper author information in commits
- **Contributors file**: Added to project contributors list
- **Release notes**: Significant contributions mentioned in releases
- **Community acknowledgment**: Recognition in project communications

### Code of Conduct

We follow the [Calypso Networks Association Code of Conduct](CODE_OF_CONDUCT.md):

- **Be respectful**: Treat all community members with respect
- **Be inclusive**: Welcome contributors from all backgrounds
- **Be collaborative**: Work together toward common goals
- **Be constructive**: Provide helpful feedback and suggestions

### Getting Help

**Technical Questions**:
- Check existing documentation first
- Search GitHub Issues for similar problems
- Create new issue with detailed problem description

**Contribution Questions**:
- Review this CONTRIBUTING guide
- Look at recent pull requests for examples
- Ask in GitHub Discussions for guidance

**Hardware/Setup Issues**:
- Consult component-specific troubleshooting sections
- Check hardware compatibility lists
- Contact CNA for proprietary plugin support

## Thank You!

Your contributions help make the Keyple Demo ecosystem better for everyone. Whether you're fixing typos, adding features, or improving documentation, every contribution is valuable and appreciated.

---

For questions about contributing, please:
1. Check this guide and existing documentation
2. Search GitHub Issues for similar questions
3. Create a new issue with the "question" label
4. Contact [Calypso Networks Association](https://calypsonet.org/contact-us/) for formal support

**Happy Contributing!** 🎉