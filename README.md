# AI Career Coach & Global Opportunity Navigator 🚀

> Intelligent Career Acceleration & Global Job Matching Platform powered by **Google Gemini 3.5 Flash**, **Spring Boot**, and **React (Vite)**.

---

## 🌟 Key Features
1. **Personalized Candidate Profiling & CV Parser**: Automatically parses PDF/Word resumes into structured technical skills, experience metrics, target roles, and relocation preferences.
2. **Global Job Matching & Gap Analysis**: Live integration with **Arbeitnow** (EU, Visa Sponsorship) and **Remotive** (Global Remote USD). Real-time matching score, skills overlap, and missing keywords.
3. **Live Gemini 3.5 Flash Deep-Dive**: In-depth evaluation of candidate fit for specific job listings, realistic immigration/visa feasibility (Singapore EP & COMPASS, Germany EU Blue Card), and predicted technical/behavioral interview questions.
4. **Resume Audit & 12-Month Progression Roadmap**: Dynamic ATS scoring, STAR-formula experience rewrites with quantifiable metrics, and prioritized milestones.
5. **Interactive AI Career Coach Chat**: Real-time conversational mentorship with context-aware candidate advice and mock interview simulation.
6. **Mobile-Responsive UI**: Optimized for smartphones, tablets, and desktop displays.

---

## 🚀 1-Click Cloud Deployment to Render

This repository includes a `render.yaml` Blueprint that configures the entire stack (Database + Backend + Frontend) automatically:

1. Sign up / Log in to [Render](https://render.com).
2. Click **New +** -> **Blueprint**.
3. Connect your GitHub repository: `https://github.com/stella30th/GLOBAL_HACKATHON_GROUP8.git`.
4. Render will detect `render.yaml` and create:
   - **aicareer-db**: Free Managed PostgreSQL Database
   - **aicareer-backend**: Dockerized Spring Boot Web Service
   - **aicareer-frontend**: High-speed Static Site with rewrite proxies
5. Click **Apply**. Within a few minutes, Render will provide a permanent public URL (e.g., `https://aicareer-frontend.onrender.com`).

---

## 💻 Local Development

1. **Start Database**:
   ```bash
   docker-compose up -d
   ```

2. **Start Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Start Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
