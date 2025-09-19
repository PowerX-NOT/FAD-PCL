# FAD - Financial Dashboard Android App

A modern Android application for personal finance management with AI-powered insights, built with Jetpack Compose and powered by GPT-5.

## Features

- 📊 **Financial Dashboard**: Track income, expenses, and balance
- 🤖 **AI-Powered Insights**: Get personalized financial advice using GPT-5
- 🏷️ **Smart Categorization**: Automatic transaction categorization
- 💡 **Budget Recommendations**: Tailored advice for students
- 📱 **Modern UI**: Built with Jetpack Compose
- 🔄 **Offline Support**: Fallback mechanisms when AI is unavailable

## Setup

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26 or higher
- GitHub account with access to GitHub Models

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd fad
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   ```
   
3. **Configure GitHub Token**
   - Get your GitHub token from [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
   - **Recommended**: Update `.env` file with your token:
     ```
     GITHUB_TOKEN=your-actual-github-token-here
     ```
   - **Alternative**: Update `gradle.properties` (less secure):
     ```
     GITHUB_TOKEN=your-actual-github-token-here
     ```
   
   The build system will prioritize the `.env` file over `gradle.properties` for better security.

4. **Build and run**
   ```bash
   ./gradlew build
   ```

## AI Integration

The app uses GitHub Models API with GPT-5 for:
- Financial insights generation
- Transaction categorization
- Personalized budget advice

### Security Notes

- **Preferred**: Store GitHub token in `.env` file (gitignored)
- **Fallback**: Token can be stored in `gradle.properties` 
- Build system prioritizes `.env` over `gradle.properties`
- Token is passed securely through BuildConfig to the app
- Never commit actual tokens to version control

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **Coroutines**: Asynchronous operations
- **StateFlow**: Reactive state management
- **Azure AI SDK**: GitHub Models integration

## Build Configuration

- **Min SDK**: 26 (required for Azure AI SDK)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Core Library Desugaring**: Enabled for Java 8+ APIs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
