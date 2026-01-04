package com.example.viaterra

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvScore: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var rbOption1: RadioButton
    private lateinit var rbOption2: RadioButton
    private lateinit var rbOption3: RadioButton
    private lateinit var rbOption4: RadioButton
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var tvFeedback: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardResult: CardView

    private var currentQuestionIndex = 0
    private var score = 0
    private val quizQuestions = getQuizQuestions()
    private val weakAreas = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        setupViews()
        loadQuestion()
    }

    private fun setupViews() {
        tvQuestion = findViewById(R.id.tvQuestion)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvScore = findViewById(R.id.tvScore)
        rgOptions = findViewById(R.id.rgOptions)
        rbOption1 = findViewById(R.id.rbOption1)
        rbOption2 = findViewById(R.id.rbOption2)
        rbOption3 = findViewById(R.id.rbOption3)
        rbOption4 = findViewById(R.id.rbOption4)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnNext = findViewById(R.id.btnNext)
        tvFeedback = findViewById(R.id.tvFeedback)
        progressBar = findViewById(R.id.progressBar)
        cardResult = findViewById(R.id.cardResult)

        btnSubmit.setOnClickListener { checkAnswer() }
        btnNext.setOnClickListener { nextQuestion() }

        rbOption1.setOnClickListener { handleOptionClick(rbOption1) }
        rbOption2.setOnClickListener { handleOptionClick(rbOption2) }
        rbOption3.setOnClickListener { handleOptionClick(rbOption3) }
        rbOption4.setOnClickListener { handleOptionClick(rbOption4) }

        findViewById<MaterialButton>(R.id.btnRetakeQuiz).setOnClickListener {
            retakeQuiz()
        }

        findViewById<MaterialButton>(R.id.btnBackToDashboard).setOnClickListener {
            finish()
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex < quizQuestions.size) {
            val question = quizQuestions[currentQuestionIndex]

            tvQuestionNumber.text = "Question ${currentQuestionIndex + 1}/${quizQuestions.size}"
            tvQuestion.text = question.question
            rbOption1.text = question.options[0]
            rbOption2.text = question.options[1]
            rbOption3.text = question.options[2]
            rbOption4.text = question.options[3]

            rbOption1.isChecked = false
            rbOption2.isChecked = false
            rbOption3.isChecked = false
            rbOption4.isChecked = false
            
            tvFeedback.visibility = View.GONE
            btnSubmit.isEnabled = false
            btnNext.visibility = View.GONE

            val progress = ((currentQuestionIndex + 1) * 100) / quizQuestions.size
            progressBar.progress = progress

        } else {
            showResults()
        }
    }

    private fun handleOptionClick(selectedRb: RadioButton) {
        rbOption1.isChecked = selectedRb.id == rbOption1.id
        rbOption2.isChecked = selectedRb.id == rbOption2.id
        rbOption3.isChecked = selectedRb.id == rbOption3.id
        rbOption4.isChecked = selectedRb.id == rbOption4.id
        
        btnSubmit.isEnabled = true
    }

    private fun checkAnswer() {
        val selectedRb = when {
            rbOption1.isChecked -> rbOption1
            rbOption2.isChecked -> rbOption2
            rbOption3.isChecked -> rbOption3
            rbOption4.isChecked -> rbOption4
            else -> null
        }
        
        if (selectedRb == null) return

        val selectedAnswer = selectedRb.text.toString()
        val question = quizQuestions[currentQuestionIndex]

        btnSubmit.isEnabled = false

        if (selectedAnswer == question.correctAnswer) {
            score++
            tvFeedback.text = "Correct! ${question.explanation}"
            tvFeedback.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvFeedback.text = "Incorrect. ${question.explanation}"
            tvFeedback.setTextColor(getColor(android.R.color.holo_red_dark))


            val category = question.category
            weakAreas[category] = (weakAreas[category] ?: 0) + 1
        }

        tvFeedback.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        tvScore.text = "Score: $score/${quizQuestions.size}"
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        loadQuestion()
    }

    private fun showResults() {
        // Hide quiz UI
        findViewById<View>(R.id.quizContainer).visibility = View.GONE
        cardResult.visibility = View.VISIBLE

        val percentage = (score * 100) / quizQuestions.size

        findViewById<TextView>(R.id.tvFinalScore).text = "$score / ${quizQuestions.size}"
        findViewById<TextView>(R.id.tvPercentage).text = "$percentage%"

        // Performance message
        val performanceMsg = when {
            percentage >= 90 -> " Great! "
            percentage >= 70 -> "Good job! "
            percentage >= 50 -> "Keep learning! "
            else -> "Please review disaster safety procedures for your safety."
        }
        findViewById<TextView>(R.id.tvPerformanceMsg).text = performanceMsg


        val recommendations = getRecommendations()
        findViewById<TextView>(R.id.tvRecommendations).text = recommendations
    }

    private fun getRecommendations(): String {
        if (weakAreas.isEmpty()) {
            return "Perfect score!"
        }

        val sortedWeakAreas = weakAreas.entries.sortedByDescending { it.value }
        val sb = StringBuilder()
        sb.append("Focus on these areas:\n\n")

        sortedWeakAreas.forEachIndexed { index, entry ->
            sb.append("${index + 1}. ${entry.key}")
            sb.append(" (${entry.value} incorrect)\n")
            sb.append("   ${getCategoryTip(entry.key)}\n\n")
        }

        return sb.toString()
    }

    private fun getCategoryTip(category: String): String {
        return when (category) {
            "Earthquake" -> "Review DROP, COVER, and HOLD ON procedures"
            "Tsunami" -> "Learn tsunami warning signs and evacuation routes"
            "Volcano" -> "Study ash fall protection and evacuation protocols"
            "Emergency Kit" -> "Check your emergency kit checklist in Safety Guide"
            "General Safety" -> "Review basic disaster preparedness principles"
            "Wildfire" -> "Learn evacuation procedures and air quality protection"
            "Flood" -> "Never walk or drive through flood water"
            else -> "Review this topic in the Safety Guide"
        }
    }

    private fun retakeQuiz() {
        currentQuestionIndex = 0
        score = 0
        weakAreas.clear()
        cardResult.visibility = View.GONE
        findViewById<View>(R.id.quizContainer).visibility = View.VISIBLE
        tvScore.text = "Score: 0/${quizQuestions.size}"
        loadQuestion()
    }

    private fun getQuizQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                category = "Earthquake",
                question = "What should you do immediately when you feel an earthquake?",
                options = listOf(
                    "Run outside as fast as possible",
                    "DROP, COVER, and HOLD ON",
                    "Stand in a doorway",
                    "Call emergency services"
                ),
                correctAnswer = "DROP, COVER, and HOLD ON",
                explanation = "DROP to your hands and knees, COVER your head under a sturdy desk or table, and HOLD ON until the shaking stops."
            ),
            QuizQuestion(
                category = "Earthquake",
                question = "If you're outdoors during an earthquake, what should you do?",
                options = listOf(
                    "Run into the nearest building",
                    "Lie flat on the ground",
                    "Move to an open area away from buildings, trees, and power lines",
                    "Hold onto a tree for support"
                ),
                correctAnswer = "Move to an open area away from buildings, trees, and power lines",
                explanation = "Move to an open space to avoid falling debris from buildings, trees, or power lines."
            ),
            QuizQuestion(
                category = "Tsunami",
                question = "What is a natural warning sign of a tsunami?",
                options = listOf(
                    "Dark clouds in the sky",
                    "Strong winds from the ocean",
                    "Ocean water rapidly receding from shore",
                    "Heavy rainfall"
                ),
                correctAnswer = "Ocean water rapidly receding from shore",
                explanation = "If you see the ocean rapidly receding or unusual ocean behavior, move to higher ground immediately - a tsunami may be coming."
            ),
            QuizQuestion(
                category = "Tsunami",
                question = "If you feel a strong earthquake near the coast, what should you do?",
                options = listOf(
                    "Wait for official tsunami warning",
                    "Go to the beach to check the ocean",
                    "Move to higher ground immediately",
                    "Stay inside and close windows"
                ),
                correctAnswer = "Move to higher ground immediately",
                explanation = "Don't wait for official warnings! A strong coastal earthquake is nature's tsunami warning. Move inland and to higher ground immediately."
            ),
            QuizQuestion(
                category = "Volcano",
                question = "What should you wear to protect yourself from volcanic ash?",
                options = listOf(
                    "Sunglasses and a hat",
                    "N-95 mask, goggles, and long-sleeved clothing",
                    "Light clothing for mobility",
                    "Waterproof jacket only"
                ),
                correctAnswer = "N-95 mask, goggles, and long-sleeved clothing",
                explanation = "Volcanic ash is harmful to breathe and irritates eyes and skin. Wear N-95 masks, goggles, and cover your skin."
            ),
            QuizQuestion(
                category = "Emergency Kit",
                question = "How much water should you store per person per day in an emergency kit?",
                options = listOf(
                    "Half a gallon",
                    "1 gallon",
                    "2 gallons",
                    "3 gallons"
                ),
                correctAnswer = "1 gallon",
                explanation = "Store at least 1 gallon of water per person per day for drinking and sanitation, for at least 3 days."
            ),
            QuizQuestion(
                category = "Emergency Kit",
                question = "How many days' worth of supplies should your emergency kit contain?",
                options = listOf(
                    "1 day",
                    "2 days",
                    "At least 3 days",
                    "1 week"
                ),
                correctAnswer = "At least 3 days",
                explanation = "Your emergency kit should contain at least 3 days of supplies. More is better, especially for areas prone to isolation."
            ),
            QuizQuestion(
                category = "General Safety",
                question = "After a major disaster, when should you use your phone?",
                options = listOf(
                    "Immediately to post on social media",
                    "Only for life-threatening emergencies",
                    "To call everyone you know",
                    "Constantly to get updates"
                ),
                correctAnswer = "Only for life-threatening emergencies",
                explanation = "Keep phone lines clear for emergency communications. Use texts instead of calls when possible, and only call 911 for emergencies."
            ),
            QuizQuestion(
                category = "Wildfire",
                question = "If wildfire smoke is affecting your area, what should you do?",
                options = listOf(
                    "Keep windows open for ventilation",
                    "Stay indoors with windows and doors closed",
                    "Exercise outdoors to build lung strength",
                    "Only wear a mask when visibility is poor"
                ),
                correctAnswer = "Stay indoors with windows and doors closed",
                explanation = "During wildfire smoke, stay indoors, close windows and doors, and use air purifiers if available. Wear N-95 masks if you must go outside."
            ),
            QuizQuestion(
                category = "Flood",
                question = "How much moving water can sweep a car away?",
                options = listOf(
                    "6 inches",
                    "1 foot",
                    "2 feet",
                    "3 feet"
                ),
                correctAnswer = "1 foot",
                explanation = "Just 1 foot of moving water can sweep away most vehicles. Never drive through flooded areas. Turn around, don't drown!"
            ),
            QuizQuestion(
                category = "Earthquake",
                question = "Should you expect aftershocks after a major earthquake?",
                options = listOf(
                    "No, earthquakes happen once",
                    "Only if it was very strong",
                    "Yes, always expect aftershocks",
                    "Only in certain areas"
                ),
                correctAnswer = "Yes, always expect aftershocks",
                explanation = "Always expect aftershocks after any earthquake. They can occur minutes, hours, or even days later and can be dangerous."
            ),
            QuizQuestion(
                category = "General Safety",
                question = "What is the universal emergency number in Pakistan?",
                options = listOf(
                    "911",
                    "999",
                    "115",
                    "100"
                ),
                correctAnswer = "115",
                explanation = "In Pakistan, dial 115 for emergency services. Also remember 1122 for Rescue Services."
            ),
            QuizQuestion(
                category = "Volcano",
                question = "Why should you clear volcanic ash from your roof?",
                options = listOf(
                    "It looks bad",
                    "It's extremely heavy when wet and can collapse roofs",
                    "It's toxic to touch",
                    "It attracts insects"
                ),
                correctAnswer = "It's extremely heavy when wet and can collapse roofs",
                explanation = "Volcanic ash becomes very heavy when wet and can cause roofs to collapse. Clear it regularly, but wear protection."
            ),
            QuizQuestion(
                category = "Emergency Kit",
                question = "Which of these is NOT essential in a basic emergency kit?",
                options = listOf(
                    "Flashlight",
                    "First aid kit",
                    "Television",
                    "Battery-powered radio"
                ),
                correctAnswer = "Television",
                explanation = "A battery-powered or hand-crank radio is essential for emergency broadcasts, but a TV is not necessary or practical."
            ),
            QuizQuestion(
                category = "General Safety",
                question = "Where is the safest place to store important documents?",
                options = listOf(
                    "In a safe deposit box or waterproof container",
                    "In a regular drawer",
                    "On your computer only",
                    "In the basement"
                ),
                correctAnswer = "In a safe deposit box or waterproof container",
                explanation = "Store important documents in waterproof, fireproof containers. Keep copies in a safe deposit box or digitally in cloud storage."
            )
        )
    }
}

data class QuizQuestion(
    val category: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)