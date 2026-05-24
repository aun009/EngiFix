# EngiFix — Project Context

## What is EngiFix?
EngiFix is an Android (Jetpack Compose) "Career OS" for engineering students. It aggregates everything needed to land a job — DSA practice, coding contest tracker, mentorship booking, job/internship listings, resume review, and a profile that syncs with all coding platforms.

## Tech Stack
- Kotlin + Jetpack Compose (UI)
- Firebase Auth + Firestore (auth & data)
- Room (local DSA progress caching)
- Retrofit + clist.by API (contest data)
- Dagger Hilt (DI)
- Coil (image loading)

## Current Features (v1)
1. **Auth flow** — Register / Login / Guest mode / AskName
2. **Home Screen** — Greeting, clock, nav cards (Jobs, Contests, Mentorship, Cognitive Game)
3. **DSA Sheets** — Striver A-Z, NeetCode150, etc. with Room-backed progress
4. **Contest Tracker** — Codeforces, CodeChef, AtCoder, LeetCode (via clist.by)
5. **Resume Roast** — PDF upload + mocked AI feedback (stub)
6. **Mentorship** — Mentor cards + Razorpay payment placeholder
7. **Jobs/Internships** — Cached job listings (Room)
8. **Profile Screen** — Avatar, bio, coding platform stats (6 platforms), DSA progress pills, logout
9. **Aptitude Game** — Cognitive reasoning drills
10. **Intro Animation** — EngiFixIntroOverlay on app launch
11. **Toolkit Screen** — 30+ quicklinks to dev resources (not in bottom nav)

## Target Users
Engineering students (India-focus) who want to flex their coding profiles, track contests, prep interviews, and showcase progress — "LinkedIn for competitive programmers".

## App Branding
- Colors: Clay (#C75F3A), Tide (#287C7A), Ink (#161616), Canvas (#F7F4EC)
- Theme: warm-paper light mode, premium minimal

## Milestone 1 — Profile Overhaul + Login Fix + Feature Gaps
Goal: make the Profile screen "goat-tier" flex-worthy, fix the guest→profile login button gap, and fill all missing edge cases.
