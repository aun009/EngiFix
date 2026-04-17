package com.campuslastday

import androidx.compose.ui.geometry.Offset

/**
 * All campus locations with IIT-flavored dialog content.
 * Canvas coordinate space: 0f..1000f on both axes.
 * The MapCanvas scales these to actual screen size at draw time.
 */
object CampusData {

    val locations: List<CampusLocation> = listOf(

        CampusLocation(
            id = LocationId.LIBRARY,
            name = "Central Library",
            center = Offset(180f, 220f),
            emoji = "📚",
            confidenceDelta = -5,
            dialogs = listOf(
                DialogNode(
                    speaker = "Your friend Arjun",
                    line = "Bhai I've been doing DP for 6 hours and I still don't get memoization. " +
                            "My brain has officially given up.",
                    response = "Same Arjun. Same."
                ),
                DialogNode(
                    speaker = "Arjun",
                    line = "Do you think they'll ask graph problems? Or trees? " +
                            "Or DP on trees? Or graphs that look like trees?",
                    response = "Arjun please stop."
                ),
                DialogNode(
                    speaker = "Notice Board",
                    line = "[Pinned] 'Placement prep session: Topic - Everything. Date - Yesterday.' ",
                    response = "Cool."
                )
            )
        ),

        CampusLocation(
            id = LocationId.PLACEMENT_CELL,
            name = "Placement Cell",
            center = Offset(500f, 180f),
            emoji = "🏢",
            confidenceDelta = -15,
            dialogs = listOf(
                DialogNode(
                    speaker = "Placement Coordinator",
                    line = "...*snoring*...",
                    response = "[Quietly leave]"
                ),
                DialogNode(
                    speaker = "Notice Board",
                    line = "TechGiant Inc — Package: 40 LPA\n" +
                            "Eligibility: CGPA ≥ 9.0, no backlogs, " +
                            "DSA round, ML round, System Design round, " +
                            "HR round, Culture Fit round, one more round.\n" +
                            "Slots: 2",
                    response = "Two slots. Cool."
                ),
                DialogNode(
                    speaker = "Random Guy Near Door",
                    line = "Bhai I heard Amazon cancelled their slot.",
                    response = "Thanks I didn't need sleep anyway."
                )
            )
        ),

        CampusLocation(
            id = LocationId.H_MESS,
            name = "H-Mess",
            center = Offset(820f, 220f),
            emoji = "🍛",
            confidenceDelta = 20,
            dialogs = listOf(
                DialogNode(
                    speaker = "Mess Uncle",
                    line = "Aaj special hai. Rajma chawal.",
                    response = "This is the only good thing happening today."
                ),
                DialogNode(
                    speaker = "Inner Voice",
                    line = "You have eaten. You are calm. You are ready. " +
                            "You have also eaten too much and now you're sleepy.",
                    response = "Worth it."
                )
            )
        ),

        CampusLocation(
            id = LocationId.CSE_DEPT,
            name = "CSE Department",
            center = Offset(180f, 550f),
            emoji = "🖥️",
            confidenceDelta = 0,   // calculated dynamically by ViewModel based on confidence
            dialogs = listOf(
                DialogNode(
                    speaker = "Prof. Sharma",
                    line = "Beta, CGPA matters. But also it doesn't. " +
                            "But also it does. Depends on the company. Also on luck. " +
                            "Also on the interviewer's mood that day.",
                    response = "Very helpful sir, thank you."
                ),
                DialogNode(
                    speaker = "Prof. Sharma",
                    line = "In my time, we wrote code on paper. " +
                            "And we got placed. No LeetCode. " +
                            "Just knowledge and hardwork.",
                    response = "Sir it's 2024."
                ),
                DialogNode(
                    speaker = "Lab Notice",
                    line = "[Project submissions due TODAY at 5 PM]\n" +
                            "[Placement Day 1 also TODAY]",
                    response = "Both. Of course it's both."
                )
            )
        ),

        CampusLocation(
            id = LocationId.GROUND,
            name = "Ground",
            center = Offset(820f, 550f),
            emoji = "🌿",
            confidenceDelta = 35,
            dialogs = listOf(
                DialogNode(
                    speaker = "The Breeze",
                    line = "...",
                    response = "[Just breathe]"
                ),
                DialogNode(
                    speaker = "A Dog Sitting Nearby",
                    line = "...*tail wag*...",
                    response = "[Pet the dog]"
                ),
                DialogNode(
                    speaker = "Inner Voice",
                    line = "You've solved 700 problems. You've pulled all-nighters. " +
                            "You've debugged code you don't remember writing. " +
                            "You've got this.",
                    response = "Ok. Ok yeah."
                )
            )
        ),

        CampusLocation(
            id = LocationId.HOSTEL_ROOM,
            name = "Your Hostel Room",
            center = Offset(500f, 600f),
            emoji = "🛏️",
            confidenceDelta = -10,
            dialogs = listOf(
                DialogNode(
                    speaker = "Roommate Rahul",
                    line = "Bhai I just solved my 4th tree problem today.",
                    response = "It's 2 AM Rahul."
                ),
                DialogNode(
                    speaker = "Rahul",
                    line = "4 trees, 2 AM. Exactly 2 trees per hour. " +
                            "I am speed-running this placement.",
                    response = "[Close laptop. Try to sleep.]"
                ),
                DialogNode(
                    speaker = "Senior's WhatsApp",
                    line = "Do graphs.\n\n" +
                            "[1 hour later]\nActually do DP.\n\n" +
                            "[30 mins later]\nForget it just do arrays.\n\n" +
                            "[2 mins later]\nDo graphs.",
                    response = "Thanks bhaiya."
                )
            )
        )
    )

    // Quick lookup by ID
    val locationMap: Map<LocationId, CampusLocation> =
        locations.associateBy { it.id }
}
