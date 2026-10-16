package com.adarsh.syllabusmission.data

// ============================================================================
// CHAPTER DATA - edit this file to change chapters. It is separate from the UI.
//
// !! NEEDS VERIFICATION !!
// These lists are written from my knowledge of the NCERT Class 10 books
// (rationalised syllabus). I could NOT check them against the current
// official CBSE/NCERT session. Compare them with the Contents page of YOUR
// textbooks. Hindi assumes Course A (Kshitij + Kritika). If you study
// Course B (Sparsh + Sanchayan), tell me and I will change it.
//
// RULES FOR EDITING:
//  - Each chapter id (e.g. "math_3") is what the saved progress uses.
//  - To add a chapter, put it at the END of its list.
//  - Do not reorder or delete chapters you have already ticked, or ticks
//    will move to different chapters.
// ============================================================================

data class Chapter(val id: String, val title: String, val section: String? = null)

data class Subject(val id: String, val name: String, val chapters: List<Chapter>)

object ChapterData {

    private fun chapters(prefix: String, section: String?, vararg titles: String): List<Chapter> =
        titles.mapIndexed { i, t -> Chapter("${prefix}_${i + 1}", t, section) }

    val subjects: List<Subject> = listOf(
        Subject(
            "math", "Mathematics",
            chapters(
                "math", null,
                "Real Numbers",
                "Polynomials",
                "Pair of Linear Equations in Two Variables",
                "Quadratic Equations",
                "Arithmetic Progressions",
                "Triangles",
                "Coordinate Geometry",
                "Introduction to Trigonometry",
                "Some Applications of Trigonometry",
                "Circles",
                "Areas Related to Circles",
                "Surface Areas and Volumes",
                "Statistics",
                "Probability"
            )
        ),
        Subject(
            "sci", "Science",
            chapters(
                "sci", null,
                "Chemical Reactions and Equations",
                "Acids, Bases and Salts",
                "Metals and Non-metals",
                "Carbon and its Compounds",
                "Life Processes",
                "Control and Coordination",
                "How do Organisms Reproduce?",
                "Heredity",
                "Light - Reflection and Refraction",
                "The Human Eye and the Colourful World",
                "Electricity",
                "Magnetic Effects of Electric Current",
                "Our Environment"
            )
        ),
        Subject(
            "sst", "Social Science",
            chapters(
                "hist", "History - India and the Contemporary World II",
                "The Rise of Nationalism in Europe",
                "Nationalism in India",
                "The Making of a Global World",
                "The Age of Industrialisation",
                "Print Culture and the Modern World"
            ) + chapters(
                "geo", "Geography - Contemporary India II",
                "Resources and Development",
                "Forest and Wildlife Resources",
                "Water Resources",
                "Agriculture",
                "Minerals and Energy Resources",
                "Manufacturing Industries",
                "Lifelines of National Economy"
            ) + chapters(
                "pol", "Political Science - Democratic Politics II",
                "Power Sharing",
                "Federalism",
                "Gender, Religion and Caste",
                "Political Parties",
                "Outcomes of Democracy"
            ) + chapters(
                "eco", "Economics - Understanding Economic Development",
                "Development",
                "Sectors of the Indian Economy",
                "Money and Credit",
                "Globalisation and the Indian Economy",
                "Consumer Rights"
            )
        ),
        Subject(
            "eng", "English",
            chapters(
                "ffp", "First Flight - Prose",
                "A Letter to God",
                "Nelson Mandela: Long Walk to Freedom",
                "Two Stories about Flying",
                "From the Diary of Anne Frank",
                "The Hundred Dresses - I",
                "The Hundred Dresses - II",
                "Glimpses of India",
                "Mijbil the Otter",
                "Madam Rides the Bus",
                "The Sermon at Benares",
                "The Proposal"
            ) + chapters(
                "ffv", "First Flight - Poetry",
                "Dust of Snow",
                "Fire and Ice",
                "A Tiger in the Zoo",
                "How to Tell Wild Animals",
                "The Ball Poem",
                "Amanda!",
                "The Trees",
                "Fog",
                "The Tale of Custard the Dragon",
                "For Anne Gregory"
            ) + chapters(
                "fwf", "Footprints Without Feet",
                "A Triumph of Surgery",
                "The Thief's Story",
                "The Midnight Visitor",
                "A Question of Trust",
                "Footprints without Feet",
                "The Making of a Scientist",
                "The Necklace",
                "Bholi",
                "The Book That Saved the Earth"
            )
        ),
        Subject(
            "hin", "Hindi",
            chapters(
                "ksh", "क्षितिज भाग 2",
                "पद (सूरदास)",
                "राम-लक्ष्मण-परशुराम संवाद (तुलसीदास)",
                "सवैया और कवित्त (देव)",
                "आत्मकथ्य (जयशंकर प्रसाद)",
                "उत्साह और अट नहीं रही है (निराला)",
                "यह दंतुरित मुसकान और फसल (नागार्जुन)",
                "छाया मत छूना (गिरिजा कुमार माथुर)",
                "कन्यादान (ऋतुराज)",
                "संगतकार (मंगलेश डबराल)",
                "नेताजी का चश्मा (स्वयं प्रकाश)",
                "बालगोबिन भगत (रामवृक्ष बेनीपुरी)",
                "लखनवी अंदाज़ (यशपाल)",
                "मानवीय करुणा की दिव्य चमक (सर्वेश्वर दयाल सक्सेना)",
                "एक कहानी यह भी (मन्नू भंडारी)",
                "स्त्री शिक्षा के विरोधी कुतर्कों का खंडन (महावीर प्रसाद द्विवेदी)",
                "नौबतखाने में इबादत (यतींद्र मिश्र)",
                "संस्कृति (भदंत आनंद कौसल्यायन)"
            ) + chapters(
                "kri", "कृतिका भाग 2",
                "माता का आँचल",
                "जॉर्ज पंचम की नाक",
                "साना-साना हाथ जोड़ि",
                "एही ठैयाँ झुलनी हेरानी हो रामा!",
                "मैं क्यों लिखता हूँ?"
            )
        )
    )

    val allIds: Set<String> by lazy { subjects.flatMap { it.chapters }.map { it.id }.toSet() }

    fun find(subjectId: String?): Subject? = subjects.firstOrNull { it.id == subjectId }
}
