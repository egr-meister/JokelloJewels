package com.jokellojewels.game.logic

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
)

/** One quiz difficulty level (stored like a category): 10 questions, 4 options, 1 correct. */
data class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val questions: List<QuizQuestion>,
)

/**
 * ============================================================================
 *  QUIZ CONTENT — "Circles & Spheres" (jewels, rings, balls, planets, geometry).
 *  3 difficulty levels × 10 single-answer questions, 4 options each.
 *  Replaceable: swap texts freely; keep the ids stable (they key saved best scores).
 * ============================================================================
 */
object QuizContent {

    val categories: List<QuizCategory> = listOf(
        QuizCategory(
            id = "easy",
            title = "Easy",
            subtitle = "Round things all around us",
            questions = listOf(
                QuizQuestion("How many corners does a circle have?", listOf("0", "1", "2", "4"), 0),
                QuizQuestion("Which 3D shape is a ball?", listOf("Cube", "Sphere", "Cone", "Pyramid"), 1),
                QuizQuestion("How many rings are on the Olympic flag?", listOf("3", "4", "5", "6"), 2),
                QuizQuestion("Which planet is famous for its bright rings?", listOf("Mars", "Venus", "Mercury", "Saturn"), 3),
                QuizQuestion("A circle cut into two equal halves gives two…", listOf("Semicircles", "Triangles", "Squares", "Rectangles"), 0),
                QuizQuestion("In which sport do you roll a ball at ten pins?", listOf("Golf", "Bowling", "Cricket", "Tennis"), 1),
                QuizQuestion("Which round object usually shows the numbers 1 to 12?", listOf("A coin", "A plate", "A clock face", "A wheel"), 2),
                QuizQuestion("What is the usual colour of a tennis ball?", listOf("Red", "Blue", "Black", "Yellow"), 3),
                QuizQuestion("Which round gem grows inside an oyster?", listOf("Pearl", "Ruby", "Emerald", "Topaz"), 0),
                QuizQuestion("How many round wheels does a bicycle have?", listOf("1", "2", "3", "4"), 1),
            ),
        ),
        QuizCategory(
            id = "medium",
            title = "Medium",
            subtitle = "Balls, games & spinning worlds",
            questions = listOf(
                QuizQuestion("The diameter of a circle is how many times its radius?", listOf("Half", "2 times", "3 times", "4 times"), 1),
                QuizQuestion("How many degrees are in a full circle?", listOf("90°", "180°", "360°", "400°"), 2),
                QuizQuestion("What is the distance all the way around a circle called?", listOf("Radius", "Diameter", "Chord", "Circumference"), 3),
                QuizQuestion("What are the small dents on a golf ball called?", listOf("Dimples", "Pits", "Craters", "Pockets"), 0),
                QuizQuestion("How many red balls are on the table at the start of a snooker frame?", listOf("10", "15", "21", "22"), 1),
                QuizQuestion("Which Greek letter names the ratio of a circle's circumference to its diameter?", listOf("Alpha", "Omega", "Pi", "Sigma"), 2),
                QuizQuestion("What is pi rounded to two decimal places?", listOf("3.41", "3.12", "3.16", "3.14"), 3),
                QuizQuestion("Counting the cue ball, how many balls start a game of 8-ball pool?", listOf("16", "15", "10", "9"), 0),
                QuizQuestion("Which of these sports uses the biggest ball?", listOf("Volleyball", "Basketball", "Handball", "Tennis"), 1),
                QuizQuestion("How long does Earth take to spin once around its axis?", listOf("About 1 hour", "About 1 week", "About 1 day", "About 1 year"), 2),
            ),
        ),
        QuizCategory(
            id = "hard",
            title = "Hard",
            subtitle = "The geometry of circles & spheres",
            questions = listOf(
                QuizQuestion("Which formula gives the area of a circle with radius r?", listOf("πr²", "2πr", "πd", "4πr²"), 0),
                QuizQuestion("Which formula gives the volume of a sphere with radius r?", listOf("πr²h", "4/3·πr³", "4πr²", "2πr³"), 1),
                QuizQuestion("What is the surface area of a sphere with radius r?", listOf("2πr²", "πr³", "4πr²", "8πr"), 2),
                QuizQuestion("A straight line that touches a circle at exactly one point is a…", listOf("Chord", "Secant", "Radius", "Tangent"), 3),
                QuizQuestion("The longest chord of any circle is its…", listOf("Diameter", "Radius", "Arc", "Sector"), 0),
                QuizQuestion("An angle inscribed in a semicircle, standing on the diameter, always measures…", listOf("45°", "90°", "120°", "180°"), 1),
                QuizQuestion("Which ancient Greek estimated pi with polygons drawn inside and outside a circle?", listOf("Pythagoras", "Euclid", "Archimedes", "Plato"), 2),
                QuizQuestion("What is the circumference of a circle with radius 5?", listOf("5π", "25π", "20π", "10π"), 3),
                QuizQuestion("Which shape holds the most volume for a given surface area?", listOf("Sphere", "Cube", "Cylinder", "Cone"), 0),
                QuizQuestion("How many radians are in a full circle?", listOf("π", "2π", "3π", "4π"), 1),
            ),
        ),
    )

    fun category(id: String?): QuizCategory? =
        if (id.isNullOrBlank()) null else categories.firstOrNull { it.id == id }
}
