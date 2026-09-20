package com.elvan.udukkai.core.navil

/**
 * Tolkappiyam Phonetic Transliteration Engine
 * (Elvan Navil Engine - Extended++ Mode)
 *
 * 100% Pure, Portable Kotlin Multiplatform implementation grounded in classical Tamil grammar:
 * - தொல்காப்பியம் எழுத்ததிகாரம் (நூன்மரபு, புள்ளிமயங்கியல், பிறப்பியல்)
 * - தொல்காப்பியம் சொல்லதிகாரம் (எச்சவியல் - நூற்பா 410, 412, 416, 420)
 * - யாப்பிலக்கணம் (அசை பிரித்தல் - Onset, Nucleus, Coda)
 * - துணைவினை & கூட்டுச்சொல் எல்லை முறைமை (Auxiliary Verb & Morpheme-Initial Stop Rule)
 *
 * Operates in the authentic standard Extended++ (Navil) phonetic mode.
 */
object NavilMozhimaatri {

    const val VIRAMA = "்"

    val VOWELS_BASE: Map<String, String> = mapOf(
        "அ" to "a",
        "ஆ" to "aa",
        "இ" to "i",
        "ஈ" to "ee",
        "உ" to "u",
        "ஊ" to "oo",
        "எ" to "e",
        "ஏ" to "ae",
        "ஐ" to "ai",
        "ஒ" to "o",
        "ஓ" to "oa",
        "ஔ" to "au"
    )

    val VOWEL_SIGNS: Map<String, String> = mapOf(
        "ா" to "aa",
        "ி" to "i",
        "ீ" to "ee",
        "ு" to "u",
        "ூ" to "oo",
        "ெ" to "e",
        "ே" to "ae",
        "ை" to "ai",
        "ொ" to "o",
        "ோ" to "oa",
        "ௌ" to "au"
    )

    // Hard stops (வல்லினம்): Word-initial, geminate, or after Vallina Mei (ற், ட்)
    // Initial ச is authentically "ch" (Chennai, Chidambaram, chiriya, chey)
    val HARD: Map<String, String> = mapOf(
        "க" to "k",
        "ச" to "ch",
        "ட" to "t",
        "த" to "th",
        "ப" to "p",
        "ற" to "r"
    )

    // Soft / Voiced stops (மெலிதல் / இடையொலி): Between vowels or after semivowels
    val SOFT: Map<String, String> = mapOf(
        "க" to "g",
        "ச" to "s",
        "ட" to "d",
        "த" to "dh",
        "ப" to "b",
        "ற" to "r"
    )

    // Geminated (இரட்டிப்பு / உடனிலை மெய்ம்மயக்கம்) in Extended++ Mode
    val DOUBLED: Map<String, String> = mapOf(
        "க" to "kk",
        "ச" to "ch",
        "ட" to "tt",
        "த" to "th",
        "ப" to "pp",
        "ற" to "tr"
    )

    // Nasals (மெல்லினம்)
    val NASAL_SOUND: Map<String, String> = mapOf(
        "ங" to "ng",
        "ஞ" to "ny",
        "ண" to "n",
        "ந" to "n",
        "ம" to "m",
        "ன" to "n"
    )

    // Homorganic pairs (இணை எழுத்துகள்)
    val INAI_EZHUTHUGAL: Map<String, String> = mapOf(
        "ங" to "க",
        "ஞ" to "ச",
        "ண" to "ட",
        "ந" to "த",
        "ம" to "ப",
        "ன" to "ற"
    )

    // Liquids and Semivowels (இடையினம்) & Grantha
    val OTHER: Map<String, String> = mapOf(
        "ய" to "y", "ர" to "r", "ல" to "l", "வ" to "v", "ழ" to "zh", "ள" to "l",
        "ஹ" to "h", "ஜ" to "j", "ஷ" to "sh", "ஸ" to "s", "ஶ" to "sh", "ஃ" to "k"
    )

    val GRANTHA: Set<String> = setOf("ஜ", "ஷ", "ஸ", "ஹ", "ஶ")
    val SEMIVOWELS: Set<String> = setOf("ய", "ர", "ல", "வ", "ழ", "ள")
    val VALLINA_MEI: Set<String> = setOf("ட்", "ற்", "க்", "ச்", "த்", "ப்")

    data class Syllable(
        val onset: String,
        val nucleus: String,
        val coda: String,
        val raw: String
    )

    data class FusionResult(
        val text: String,
        val rule: String
    )

    fun isVowelSign(ch: String?): Boolean = ch != null && VOWEL_SIGNS.containsKey(ch)
    fun isStop(ch: String?): Boolean = ch != null && HARD.containsKey(ch)
    fun isNasal(ch: String?): Boolean = ch != null && NASAL_SOUND.containsKey(ch)
    fun isConsonant(ch: String?): Boolean = isStop(ch) || isNasal(ch) || (ch != null && OTHER.containsKey(ch))
    fun isSpace(ch: String?): Boolean = ch == null || ch.matches(Regex("[\\s\\n\\r]"))
    fun isPunct(ch: String?): Boolean = ch != null && ch.matches(Regex("[.,!?;:)\\]}\\-—\"']"))

    /**
     * Identifies roots and words where the nasal-stop sequence (specifically ன்/ண் + ப)
     * undergoes historical/morphological voicing (b) vs compound fortis (p).
     */
    fun isVoicedNbWord(word: String): Boolean {
        // 1. Time words with -பு (must be munbu / pinbu, not munpakkam or pinpaattu)
        val timeExcludes = listOf("முன்பக்க", "பின்பக்க", "பின்பாட்டு", "முன்பகுதி", "பின்பகுதி", "முன்பல்", "பின்பல்", "முன்பார்வை", "பின்பார்வை")
        if (word.startsWith("முன்ப") || word.startsWith("பின்ப")) {
            if (timeExcludes.none { word.startsWith(it) }) return true
        }
        if (word.contains("முன்ப") || word.contains("பின்ப")) {
            if (timeExcludes.none { word.contains(it) }) return true
        }

        // 2. Noun roots (anbu, panbu, nanban, inbam, thunbam, enbu)
        if (word.contains("அன்ப")) return true
        if (word.contains("பண்ப")) return true
        if (word.contains("நண்பன்") || word.contains("நண்பர்") || word.contains("நண்பா")) return true
        if (word.contains("இன்ப")) return true
        if (word.contains("துன்ப")) return true
        if ((word.contains("என்பு") || word.contains("என்பி")) && !word.contains("என்பின்")) return true
        if (word.contains("மன்பதை") || word.contains("தென்படு")) return true

        // 3. Numbers with -பது (enbadhu, onbadhu, pathonbadhu, enbaan, onbaan)
        if (word.contains("எண்பத") || word.contains("ஒன்பத") || word.contains("தொன்பத") || word.contains("எண்பா") || word.contains("ஒன்பா")) return true

        // 4. Verbs with future tense -ப- (unbaan, kaanbaan, thinbaan, poonbaan)
        if (word.contains("உண்ப") || word.contains("காண்ப") || word.contains("திண்ப") || word.contains("பூண்ப")) return true

        // 5. Verb: enbaan, enbaar, enbadhu (to say) vs noun என் (my: enpakkam, enpadi, enpoal)
        if (word.contains("என்பத") || word.contains("என்பா") || word.contains("என்பர்") || word.contains("என்பன")) {
            if (!word.contains("என்பார்வை") && !word.contains("என்பாடம்") && !word.contains("என்பாடு") && !word.contains("என்பொருள்")) {
                return true
            }
        }

        return false
    }

    /**
     * Checks if a stop consonant at position j is the onset of an auxiliary verb (துணைவினை)
     * or a compound word head (வருமொழி முதனிலை), which preserves its initial unvoiced stop.
     */
    fun isMorphemeInitialStop(chars: List<String>, j: Int, wordStr: String, startIdx: Int = 0): Boolean {
        val c = chars[j]
        val remaining = chars.subList(j, chars.size).joinToString("")
        val prefix = chars.subList(startIdx, j).joinToString("")

        // 1. Numerical Prefixes (எண்ணடை முன்னொட்டுகள்: ஒரு, இரு, அறு)
        if (prefix == "இரு" || prefix == "ஒரு" || prefix == "அறு" ||
            prefix.endsWith("இரு") || prefix.endsWith("ஒரு") || prefix.endsWith("அறு")) {
            if (c == "ப" && remaining.startsWith("பது")) {
                return false // இருபது -> irubadhu
            }
            return true
        }

        // 2. Auxiliary Verbs & Compound Heads starting with 'க'
        if (c == "க") {
            if (remaining.startsWith("கூட")) return true
        }

        // 3. Auxiliary Verbs & Compound Heads starting with 'ப'
        if (c == "ப") {
            if (Regex("^பெ(?:ற்ற|ற்று|ற|று)").containsMatchIn(remaining)) return true
            if (remaining.startsWith("பிற")) return true
            if (Regex("^ப(?:ட்ட|ட்டு|ட|டு)").containsMatchIn(remaining)) return true
            if (remaining.startsWith("பிடி")) return true
            if (remaining.startsWith("பார்")) return true
            if (Regex("^போ(?:ட்ட|ட்டு|ட|டு)").containsMatchIn(remaining)) return true
            if (Regex("^போ(?:ல|ல்|ன்ற|ன்று)").containsMatchIn(remaining)) return true
            if (Regex("^(?:பொரு|புக|படை|பகுதி|பக்கம்|பாட்டு|பால்|பாண்ட)").containsMatchIn(remaining)) return true
        }

        return false
    }

    /**
     * Resolves nasal + stop fusion according to Tolkappiyam homorganic/heterogeneous rules.
     */
    fun getFusion(
        nasal: String,
        stop: String,
        stopHasVirama: Boolean = false,
        vowelChar: String = "",
        isGrantha: Boolean = false,
        wordStr: String = ""
    ): FusionResult {
        if (stopHasVirama) {
            if (nasal == "ஞ" && stop == "ச") return FusionResult("nch", "இணை எழுத்து ஒற்று (ஞ்+ச்)")
            if (nasal == "ன" && stop == "ற") return FusionResult("ntr", "இணை எழுத்து ஒற்று (ன்+ற்)")
            if (nasal == "ந" && stop == "ற") return FusionResult("ntr", "இணை எழுத்து ஒற்று (ந்+ற்)")
            if (nasal == "ங" && stop == "க") return FusionResult("nk", "இணை எழுத்து ஒற்று (ங்+க்)")
            return FusionResult((NASAL_SOUND[nasal] ?: "n") + (HARD[stop] ?: stop), "மெல்லின ஒற்று + வல்லின ஒற்று")
        }

        if (isGrantha) {
            return FusionResult((NASAL_SOUND[nasal] ?: "n") + (HARD[stop] ?: stop), "கிரந்த ஒலி (Grantha Hard Preservation)")
        }

        // 1. Homorganic pairs voice (இணை எழுத்துகள்: ங்க, ஞ்ச, ண்ட, ந்த, ம்ப, ன்ற)
        if (nasal == "ங" && stop == "க") return FusionResult("ng", "இணை எழுத்து (ங் + க -> ng)")
        if (nasal == "ஞ" && stop == "ச") return FusionResult("nj", "இணை எழுத்து (ஞ் + ச -> nj)")
        if (nasal == "ண" && stop == "ட") return FusionResult("nd", "இணை எழுத்து (ண் + ட -> nd)")
        if (nasal == "ந" && stop == "த") return FusionResult("ndh", "இணை எழுத்து (ந் + த -> ndh)")
        if (nasal == "ம" && stop == "ப") return FusionResult("mb", "இணை எழுத்து (ம் + ப -> mb)")
        if (nasal == "ன" && stop == "ற") return FusionResult("ndr", "இணை எழுத்து (ன் + ற -> ndr)")
        if (nasal == "ந" && stop == "ற") return FusionResult("ndr", "இணை எழுத்து (ந் + ற -> ndr)")

        // 2. Non-Homorganic ன்/ண் + ப
        if ((nasal == "ன" || nasal == "ண") && stop == "ப") {
            if (isVoicedNbWord(wordStr)) {
                return FusionResult((NASAL_SOUND[nasal] ?: "n") + "b", "பகாப்பதம்/வினை இடைநிலை மெலிதல் (nb)")
            }
            return FusionResult((NASAL_SOUND[nasal] ?: "n") + "p", "தொகைச்சொல் வருமொழி முதனிலை (np)")
        }

        // Emphatic suffix -தான் / -தானே after ன் (அவன்தான், நான்தான், இவன்தான் -> ndhaan)
        if (nasal == "ன" && stop == "த") {
            if (wordStr.endsWith("தான்") || wordStr.endsWith("தானே") || wordStr.contains("தான்") || wordStr.contains("தானே")) {
                return FusionResult("ndh", "தேற்ற ஏகார இடைச்சொல் (ndhaan)")
            }
        }

        // 3. Plural suffix -கள் after ண்/ன் (கண்கள் -> kangal, பெண்கள் -> pengal, எண்கள் -> engal)
        if ((nasal == "ண" || nasal == "ன") && stop == "க") {
            val isPluralSuffix = wordStr.endsWith("கள்") || wordStr.contains("கள")
            if (isPluralSuffix) {
                return FusionResult((NASAL_SOUND[nasal] ?: "n") + "g", "பன்மை விகுதி (-கள் -> gal)")
            }
        }

        return FusionResult((NASAL_SOUND[nasal] ?: "n") + (HARD[stop] ?: stop), "வேற்று மெல்லின மயக்கம் (Hard Stop)")
    }

    /**
     * Cleanses erroneous doubled consonants after hard stops (e.g. பொற்க்காசு -> பொற்காசு, முயற்சி -> முயற்சி).
     */
    fun sanitizeRedundantVallinaMei(text: String): String {
        if (text.isEmpty()) return ""
        val chars = text.map { it.toString() }
        val sanitized = StringBuilder()

        var i = 0
        while (i < chars.size) {
            sanitized.append(chars[i])

            if (i >= 1 && chars[i] == VIRAMA && (chars[i - 1] == "ற" || chars[i - 1] == "ட")) {
                val nextChar = chars.getOrNull(i + 1)
                val nextNext = chars.getOrNull(i + 2)
                if (nextChar != null && nextNext == VIRAMA && isStop(nextChar)) {
                    i += 2
                }
            }
            i++
        }

        return sanitized.toString()
    }

    /**
     * Splits a Tamil word into classical metric syllables (அசை பிரித்தல்)
     * based on Onset (தொடக்க மெய்), Nucleus (உயிர்/உயிர்மெய்), and Coda (ஈற்று மெய்).
     */
    fun splitSyllables(word: String): List<Syllable> {
        val chars = word.map { it.toString() }
        val syllables = mutableListOf<Syllable>()
        var i = 0

        while (i < chars.size) {
            var onset = ""
            var nucleus = ""
            var coda = ""
            val raw = StringBuilder()

            if (VOWELS_BASE.containsKey(chars[i])) {
                nucleus = chars[i]
                raw.append(chars[i])
                i++
            } else if (isConsonant(chars[i])) {
                onset = chars[i]
                raw.append(chars[i])
                i++

                if (chars.getOrNull(i) == VIRAMA) {
                    coda = onset
                    onset = ""
                    raw.append(chars[i])
                    i++
                    syllables.add(Syllable(onset = "", nucleus = "", coda = coda, raw = raw.toString()))
                    continue
                }

                if (isVowelSign(chars.getOrNull(i))) {
                    nucleus = chars[i]
                    raw.append(chars[i])
                    i++
                } else {
                    nucleus = "அ"
                }
            } else {
                raw.append(chars[i])
                i++
                syllables.add(Syllable(onset = "", nucleus = "", coda = "", raw = raw.toString()))
                continue
            }

            if (isConsonant(chars.getOrNull(i)) && chars.getOrNull(i + 1) == VIRAMA) {
                coda = chars[i]
                raw.append(chars[i]).append(chars[i + 1])
                i += 2
            }

            syllables.add(Syllable(onset = onset, nucleus = nucleus, coda = coda, raw = raw.toString()))
        }

        return syllables
    }

    /**
     * Primary transliteration function: Converts Tamil text to Latin/Romanized phonetic script
     * according to the 14 Tolkappiyam rules in Extended++ (Navil) Mode.
     *
     * Rule 14 (Title Case / Word Capitalization) is enabled by default for optimal readability.
     * Pass [capitalizeWords] = false to obtain raw lowercase phonetics.
     */
    fun transliterate(text: String, capitalizeWords: Boolean = true): String {
        if (text.isEmpty()) return ""

        val cleanText = sanitizeRedundantVallinaMei(text)
        val chars = cleanText.map { it.toString() }
        val out = StringBuilder()
        var i = 0

        fun processWord(startIdx: Int, endIdx: Int): String {
            val wordOut = StringBuilder()
            var prevWasVowel = false
            val wordStr = chars.subList(startIdx, endIdx + 1).joinToString("")

            var isGranthaWord = false
            for (k in startIdx..endIdx) {
                if (GRANTHA.contains(chars[k])) {
                    isGranthaWord = true
                    break
                }
            }

            var j = startIdx
            while (j <= endIdx) {
                val c = chars[j]
                val isStart = (j == startIdx)

                val n1 = chars.getOrNull(j + 1)
                val n2 = chars.getOrNull(j + 2)
                val n3 = chars.getOrNull(j + 3)

                // Independent Vowels
                if (VOWELS_BASE.containsKey(c)) {
                    val v = VOWELS_BASE[c] ?: ""
                    wordOut.append(v)
                    prevWasVowel = true
                    j++
                    continue
                }

                if (c == VIRAMA) {
                    j++
                    continue
                }

                // Special handling for Aaytham (ஃ)
                if (c == "ஃ") {
                    wordOut.append("h")
                    j++
                    continue
                }

                // Pattern 1: Nasal + virama + stop
                if (isNasal(c) && n1 == VIRAMA && isStop(n2)) {
                    val stopHasVirama = (n3 == VIRAMA)
                    val vowel = if (isVowelSign(n3)) VOWEL_SIGNS[n3] ?: "" else if (n3 == VIRAMA) "" else "a"
                    val fusionResult = getFusion(c, n2!!, stopHasVirama, vowel, isGranthaWord, wordStr)

                    val skip = if (isVowelSign(n3)) 4 else if (n3 == VIRAMA) 4 else 3
                    wordOut.append(fusionResult.text).append(vowel)
                    j += skip
                    prevWasVowel = vowel.isNotEmpty()
                    continue
                }

                // Pattern 2: Geminate in Extended++ mode
                if (isConsonant(c) && n1 == VIRAMA && n2 == c) {
                    val baseSound = HARD[c] ?: NASAL_SOUND[c] ?: OTHER[c] ?: c
                    val gem = when (c) {
                        "ச" -> "ch"
                        "த" -> "th"
                        "ய" -> "iy"
                        else -> DOUBLED[c] ?: (baseSound + baseSound)
                    }

                    val vowel = if (isVowelSign(n3)) VOWEL_SIGNS[n3] ?: "" else if (n3 == VIRAMA) "" else "a"
                    val skip = if (isVowelSign(n3)) 4 else if (n3 == VIRAMA) 4 else 3

                    wordOut.append(gem).append(vowel)
                    j += skip
                    prevWasVowel = vowel.isNotEmpty()
                    continue
                }

                // Pattern 3: Regular Consonants & Stops
                if (isConsonant(c)) {
                    var sound = ""

                    if (isStop(c)) {
                        val prevChar = if (j > startIdx) chars[j - 1] else null
                        val prevPrevChar = if (j > startIdx + 1) chars[j - 2] else null

                        val afterAaydham = (prevChar == "ஃ")
                        val afterVallinaMei = (prevChar == VIRAMA && prevPrevChar != null && (prevPrevChar == "ற" || prevPrevChar == "ட"))
                        val afterSemivowelPulli = (prevChar == VIRAMA && prevPrevChar != null && SEMIVOWELS.contains(prevPrevChar))
                        val afterSemivowelDirect = (prevChar != null && SEMIVOWELS.contains(prevChar) && !isVowelSign(prevChar) && prevChar != VIRAMA)

                        val isMorphemeInitial = isMorphemeInitialStop(chars, j, wordStr, startIdx)

                        sound = when {
                            afterAaydham -> if (c == "ற") "tr" else (HARD[c] ?: "")
                            afterVallinaMei -> HARD[c] ?: ""
                            isStart || isMorphemeInitial -> HARD[c] ?: ""
                            n1 == VIRAMA -> HARD[c] ?: ""
                            (prevWasVowel || afterSemivowelPulli || afterSemivowelDirect) && !isGranthaWord -> SOFT[c] ?: ""
                            else -> HARD[c] ?: ""
                        }
                    } else {
                        sound = NASAL_SOUND[c] ?: OTHER[c] ?: c
                    }

                    val vowel = if (isVowelSign(n1)) VOWEL_SIGNS[n1] ?: "" else if (n1 == VIRAMA) "" else "a"
                    val skip = if (isVowelSign(n1)) 2 else if (n1 == VIRAMA) 2 else 1

                    if (c == "ய" && n1 == VIRAMA) {
                        sound = "i"
                    }

                    wordOut.append(sound).append(vowel)
                    j += skip
                    prevWasVowel = vowel.isNotEmpty()
                    continue
                }

                wordOut.append(c)
                j++
            }

            return wordOut.toString()
        }

        while (i < chars.size) {
            if (isSpace(chars[i]) || isPunct(chars[i])) {
                out.append(chars[i])
                i++
            } else {
                val start = i
                while (i < chars.size && !isSpace(chars[i]) && !isPunct(chars[i])) {
                    i++
                }
                out.append(processWord(start, i - 1))
            }
        }

        val rawResult = out.toString()
        return if (capitalizeWords) capitalizeWords(rawResult) else rawResult
    }

    /**
     * Capitalizes the first alphabetical character of a string, preserving any leading quotes/brackets.
     */
    fun capitalizeFirstLetter(text: String): String {
        if (text.isEmpty()) return ""
        val regex = Regex("^([\\s\"'“”‘’(«\\[]*)([a-z])", RegexOption.IGNORE_CASE)
        return regex.replace(text) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val letter = matchResult.groupValues[2]
            prefix + letter.uppercase()
        }
    }

    /**
     * Standard English/Latin sentence-case capitalizer (வாக்கியத் தொடக்கப் பேரெழுத்து).
     */
    fun capitalizeSentences(text: String): String {
        if (text.isEmpty()) return ""
        val regex = Regex("(^|[.?!]\\s+|\\r?\\n\\s*)([\\s\"'“”‘’(«\\[]*)([a-z])", RegexOption.IGNORE_CASE)
        return regex.replace(text) { matchResult ->
            val boundary = matchResult.groupValues[1]
            val punct = matchResult.groupValues[2]
            val letter = matchResult.groupValues[3]
            boundary + punct + letter.uppercase()
        }
    }

    /**
     * Applies Latin poetic line-initial capitalization to poem text.
     */
    fun capitalizePoem(text: String): String {
        if (text.isEmpty()) return ""
        val lines = text.split("\n")
        return lines.joinToString("\n") { line ->
            capitalizeFirstLetter(line)
        }
    }

    /**
     * Capitalizes every word in the text (Title Case / சொல் தொடக்கப் பேரெழுத்து).
     * Facilitates Romanized Tamil visual word segmentation for optimal readability.
     */
    fun capitalizeWords(text: String): String {
        if (text.isEmpty()) return ""
        val regex = Regex("\\b([a-z])")
        return regex.replace(text) { matchResult ->
            matchResult.groupValues[1].uppercase()
        }
    }
}
