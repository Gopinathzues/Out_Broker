# OutBroker

**A property marketplace platform connecting property owners and users directly.**

OutBroker aims to simplify property discovery and communication without relying on traditional brokers.

## Project Overview

OutBroker is a full-stack web application built with a React frontend and Spring Boot backend. It includes property-related workflows and authentication features.

## Tech Stack

### Frontend
- React
- TypeScript
- Vite

### Backend
- Java
- Spring Boot
- Spring Security
- JWT authentication

### Database
- PostgreSQL

## Main Features

Features implemented in the repository may include:

- User authentication and OTP verification
- Property listing and browsing
- Property search
- Property management
- Image uploads
- Wishlist and favorites
- Reviews and inquiries
- Booking-related workflows
- Chat and notifications
- Admin and property verification workflows

*Availability and completion of individual features should be verified against the current code.*

## Repository Structure

```text
Out_Broker/
├── backend/
├── frontend/
├── .env.example
├── .gitignore
└── README.md
```

## Prerequisites

Install the following tools before attempting to run the full application:

- Git
- Java JDK version required by the backend build configuration
- Node.js LTS and npm
- PostgreSQL with any required extensions

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Gopinathzues/Out_Broker.git
cd Out_Broker
```

### 2. Frontend setup

```bash
cd frontend
npm install
npm run
```

The second command lists available npm scripts. If a `dev` script is present, start the development server with:

```bash
npm run dev
```

Open the local URL printed in the terminal.

### 3. Backend setup

Before starting the backend:

1. Check the backend build files and Java version requirement.
2. Create and configure a local PostgreSQL database.
3. Configure the required environment variables.
4. Follow the backend build/run instructions supported by the repository.

The backend configuration uses variables such as `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`. Production configuration also uses `APP_ALLOWED_ORIGINS`.

**Never commit real credentials or share production secrets.** Use local development values.

## Environment Configuration

Refer to `.env.example` for placeholder configuration values.

Do not copy real passwords, JWT secrets, API keys, or production database credentials into GitHub.

## Contributing

Contributions and code reviews are welcome.

1. Review the existing code before making changes.
2. Report bugs with reproduction steps.
3. Discuss proposed changes with the repository owner.
4. Use a separate branch or fork for agreed changes.
5. Submit changes for review before merging into `main`.

## Security

- Never commit secrets or personal data.
- Do not use production credentials for local testing.
- Report security issues privately to the repository owner.

## Project Status

This project is under development. Features and setup instructions may change as development progresses.
