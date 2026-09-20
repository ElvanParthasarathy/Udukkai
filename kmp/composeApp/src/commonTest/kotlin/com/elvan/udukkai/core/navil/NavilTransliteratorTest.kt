package com.elvan.udukkai.core.navil

import kotlin.test.Test
import kotlin.test.assertEquals

class NavilMozhimaatriTest {

    data class TestCase(val input: String, val expected: String, val note: String = "")

    @Test
    fun testTolkappiyamPhoneticSuite() {
        val tests = listOf(
            // 1. Vallina Mei (வல்லின மெய்களுக்குப் பின் ஒற்று மிகாமை)
            TestCase("முயற்சி", "muyarchi", "Hard ch after ற்"),
            TestCase("முயற்ச்சி", "muyarchi", "Auto-cleanses redundant ச்"),
            TestCase("பொற்காசு", "porkaasu", "Hard k after ற்"),
            TestCase("பொற்க்காசு", "porkaasu", "Auto-cleanses redundant க்"),
            TestCase("கற்பனை", "karpanai", "Hard p after ற்"),
            TestCase("காட்சி", "kaatchi", "Hard ch after ட்"),
            TestCase("வெட்கம்", "vetkam", "Hard k after ட்"),
            TestCase("நட்பு", "natpu", "Hard p after ட்"),

            // 2. Inai Ezhuthugal (இணை எழுத்துகள்)
            TestCase("தங்கம்", "thangam", "ங் + க -> ng"),
            TestCase("பஞ்சு", "panju", "ஞ் + ச -> nj"),
            TestCase("வண்டு", "vandu", "ண் + ட -> nd"),
            TestCase("பந்து", "pandhu", "ந் + த -> ndh"),
            TestCase("அம்பு", "ambu", "ம் + ப -> mb"),
            TestCase("கன்று", "kandru", "ன் + ற -> ndr"),

            // 3. பகாப்பதம் & Voiced Roots/Verbs
            TestCase("அன்பு", "anbu", "Noun root voicing"),
            TestCase("பண்பு", "panbu", "Noun root voicing"),
            TestCase("நண்பன்", "nanban", "Noun root voicing"),
            TestCase("இன்பம்", "inbam", "Noun root voicing"),
            TestCase("துன்பம்", "thunbam", "Noun root voicing"),
            TestCase("என்பு", "enbu", "Noun root (bone)"),
            TestCase("முன்பு", "munbu", "Time/location suffix"),
            TestCase("பின்பு", "pinbu", "Time/location suffix"),
            TestCase("உண்பான்", "unbaan", "Verb future tense"),
            TestCase("காண்பான்", "kaanbaan", "Verb future tense"),
            TestCase("என்பார்", "enbaar", "Verb future tense"),

            // 4. தொகைச்சொற்கள் (Compounds - second word keeps initial hard 'p')
            TestCase("வான்புகழ்", "vaanpugazh", "புகழ் keeps p"),
            TestCase("வான்புகழும்", "vaanpugazhum", "புகழ் keeps p (inflected)"),
            TestCase("மென்பொருள்", "menporul", "பொருள் keeps p"),
            TestCase("வன்பொருள்", "vanporul", "பொருள் keeps p"),
            TestCase("கண்பார்வை", "kanpaarvai", "பார்வை keeps p"),
            TestCase("வான்படை", "vaanpadai", "படை keeps p"),
            TestCase("முன்பக்கம்", "munpakkam", "பக்கம் keeps p"),
            TestCase("பின்பாட்டு", "pinpaattu", "பாட்டு keeps p"),
            TestCase("மின்பகுப்பு", "minpaguppu", "பகுப்பு keeps p"),
            TestCase("பொன்பூ", "ponpoo", "பூ keeps p"),
            TestCase("நண்பகல்", "nanpagal", "பகல் keeps p"),

            // 5. Semivowels / Liquids (இடையினம்)
            TestCase("பல்கலை", "palgalai", "Softens after ல்"),
            TestCase("இயல்பு", "iyalbu", "Softens after ல் (ப -> b)"),
            TestCase("இயல்பும்", "iyalbum", "Softens after ல் (ப -> b, with -um)"),
            TestCase("அவர்கள்", "avargal", "Softens after ர்"),
            TestCase("வாழ்க", "vaazhga", "Softens after ழ்"),

            // 6. பன்மை விகுதி (-கள் -> gal / kal)
            TestCase("கண்கள்", "kangal", "Plural gal after ண்"),
            TestCase("பெண்கள்", "pengal", "Plural gal after ண்"),
            TestCase("எண்கள்", "engal", "Plural gal after ண்"),
            TestCase("கால்கள்", "kaalgal", "Plural gal after ல்"),
            TestCase("நூல்கள்", "noolgal", "Plural gal after ல்"),
            TestCase("கற்கள்", "karkal", "Plural kal after ற் (Hard)"),
            TestCase("நாட்கள்", "naatkal", "Plural kal after ட் (Hard)"),

            // 7. Authentic Initial ச (ch)
            TestCase("சென்னை", "chennai", "Authentic affricate ch"),
            TestCase("சாப்பாடு", "chaappaadu", "Authentic affricate ch"),
            TestCase("சரி", "chari", "Authentic affricate ch"),
            TestCase("சிறிய", "chiriya", "Authentic affricate ch"),

            // 8. Geminates (இரட்டிப்பு)
            TestCase("பச்சை", "pachai", "Geminate ch"),
            TestCase("அப்பா", "appaa", "Geminate pp"),
            TestCase("காற்று", "kaatru", "Geminate tr"),

            // 9. துணைவினைகள் & கூட்டுச்சொல் எல்லை
            TestCase("நிலைபெற்றுள்ள", "nilaipetrulla", "பெறு stem keeps hard p"),
            TestCase("நிலைப்பெற்றுள்ள", "nilaippetrulla", "Geminate pp preserved"),
            TestCase("நடைபெறும்", "nadaiperum", "பெறு stem keeps hard p"),
            TestCase("வெற்றிபெற்றார்", "vetripetraar", "பெறு stem keeps hard p"),
            TestCase("கண்டுபிடி", "kandupidi", "பிடி stem keeps hard p"),
            TestCase("தொன்றுதொட்டு", "thondrudhottu", "உ + த intervocalic -> dh"),

            // 10. எண்ணடை முன்னொட்டுகள் vs உரிச்சொல்
            TestCase("இருபெரும்", "iruperum", "இரு prefix keeps hard p"),
            TestCase("ஒருபக்கம்", "orupakkam", "ஒரு prefix keeps hard p"),
            TestCase("ஒருபொழுதும்", "orupozhudhum", "ஒரு prefix keeps hard p"),
            TestCase("இருபக்கம்", "irupakkam", "இரு prefix keeps hard p"),
            TestCase("இருபது", "irubadhu", "-பது tens suffix voices to badhu"),
            TestCase("மாபெரும்", "maaberum", "மா- before single stop voices to b"),

            // 11. உவம உருபுகள் (போல், போல)
            TestCase("தூறல்போல்", "thooralpoal", "போல் suffix keeps hard p"),
            TestCase("மழைபோல்", "mazhaipoal", "போல் suffix keeps hard p"),
            TestCase("அலைபோல", "alaipoala", "போல suffix keeps hard p"),

            // 12. புணர்ச்சி & இடைச்சொல்
            TestCase("முன்பில்லா", "munbillaa", "முன்பு + இல்லா -> munbillaa"),
            TestCase("முன்பெல்லாம்", "munbellaam", "முன்பு + எல்லாம் -> munbellaam"),
            TestCase("முன்பிருந்த", "munbirundha", "முன்பு + இருந்த -> munbirundha"),
            TestCase("அவள்கூட", "avalkooda", "கூட postposition keeps hard k"),
            TestCase("நான்கூட", "naankooda", "கூட postposition keeps hard k"),
            TestCase("அவன்தான்", "avandhaan", "ன் + தான் -> ndhaan"),
            TestCase("அவள்தான்", "avaldhaan", "ள் + தான் -> ldhaan"),
            TestCase("நான்தான்", "naandhaan", "ன் + தான் -> ndhaan"),
            TestCase("கண்திறந்து", "kanthirandhu", "ண் + திறந்து keeps hard th"),
            TestCase("மண்பானை", "manpaanai", "ண் + பானை keeps hard p"),
            TestCase("நாள்தோறும்", "naaldhoarum", "ள் + தோறும் softens to ldhoarum"),
            TestCase("செய்தான்", "cheidhaan", "ய் + தான் voices to idhaan"),
            TestCase("வருவான்", "varuvaan", "வருவான் standard"),

            // 13. எண்கள் (-பது ஈறு) & என் வேற்றுமை vs வினை
            TestCase("எண்பது", "enbadhu", "எண் + பது -> enbadhu"),
            TestCase("ஒன்பது", "onbadhu", "ஒன் + பது -> onbadhu"),
            TestCase("பத்தொன்பது", "pathonbadhu", "பத்து + ஒன்பது -> pathonbadhu"),
            TestCase("என்பக்கம்", "enpakkam", "என் + பக்கம் (noun) keeps hard p"),
            TestCase("என்படி", "enpadi", "என் + படி (postposition) keeps hard p"),
            TestCase("என்போல்", "enpoal", "என் + போல் (postposition) keeps hard p"),
            TestCase("என்பார்வை", "enpaarvai", "என் + பார்வை (noun) keeps hard p"),
            TestCase("என்பது", "enbadhu", "என் + பது (verb) voices to b"),

            // 14. ஆய்த எழுத்து (ஃ) பின் வல்லினம் (தொல்காப்பியம் 38)
            TestCase("எஃகு", "ehku", "ஃ + கு -> ehku (hard k)"),
            TestCase("அஃது", "ahthu", "ஃ + து -> ahthu (hard th)"),
            TestCase("இஃது", "ihthu", "ஃ + து -> ihthu (hard th)"),
            TestCase("அஃறிணை", "ahtrinai", "ஃ + றி -> ahtrinai (hard tr)"),
            TestCase("பஃறுளி", "pahtruli", "ஃ + று -> pahtruli (hard tr)"),
            TestCase("கஃசு", "kahchu", "ஃ + சு -> kahchu (hard ch)"),

            // 15. வருமொழி முதனிலை & முன்னொட்டு
            TestCase("மறுபிறவி", "marupiravi", "மறு + பிறவி -> marupiravi (hard p)"),
            TestCase("மறுபிறப்பு", "marupirappu", "மறு + பிறப்பு -> marupirappu (hard p)"),

            // 16. உறுதிசெய்யப்பட்ட பிற விதிகள்
            TestCase("வந்தபோது", "vandhaboadhu", "வந்த + போது -> vandhaboadhu (natural b)"),
            TestCase("நான்கு", "naanku", "ன் + கு -> naanku (hard k)"),
            TestCase("கண்காணி", "kankaani", "ண் + கா -> kankaani (hard k)")
        )

        for (test in tests) {
            val actual = NavilMozhimaatri.transliterate(test.input, capitalizeWords = false)
            assertEquals(test.expected, actual, "Failed on '${test.input}' (${test.note})")
        }
    }

    @Test
    fun testCapitalization() {
        // 1. Default transliterate has Rule 14 (capitalizeWords = true) always ON
        val words = NavilMozhimaatri.transliterate("இன்றைய பல்கலைக்கழகத்தில் நடைபெற்ற செம்மொழித் தமிழாய்வுக் கருத்தரங்கில்")
        assertEquals("Indraiya Palgalaikkazhagathil Nadaipetra Chemmozhith Thamizhaaivuk Karutharangil", words)

        // 2. Can be turned OFF by passing capitalizeWords = false
        val raw = NavilMozhimaatri.transliterate("முயற்சி", capitalizeWords = false)
        assertEquals("muyarchi", raw)

        // 3. Sentence case
        val sent = NavilMozhimaatri.capitalizeSentences(NavilMozhimaatri.transliterate("வணக்கம். எப்படி இருக்கிறீர்கள்? நன்றாக இருக்கிறேன்!", capitalizeWords = false))
        assertEquals("Vanakkam. Eppadi irukkireergal? Nandraaga irukkiraen!", sent)

        // 4. Poem case
        val poem = NavilMozhimaatri.capitalizePoem(NavilMozhimaatri.transliterate("தொன்றுதொட்டு நிலைபெற்றுள்ள\nஇருபெரும் கவிஞர்கள்\nஇயல்பும் புகழும் பெற்றார்", capitalizeWords = false))
        assertEquals("Thondrudhottu nilaipetrulla\nIruperum kavinyargal\nIyalbum pugazhum petraar", poem)
    }

    @Test
    fun testSyllableSplitter() {
        val word = "முயற்சி"
        val syllables = NavilMozhimaatri.splitSyllables(word).map { it.raw }.filter { it.isNotEmpty() }
        assertEquals(listOf("மு", "யற்", "சி"), syllables)
    }

    @Test
    fun testDynamicTaLatnTransliteration() {
        val senthamizh = com.elvan.udukkai.localization.language_keys.taLatn[com.elvan.udukkai.localization.K.pureTamil]
        kotlin.test.assertNotNull(senthamizh)
        assertEquals("Chendhamizh", senthamizh)

        val niruvanam = com.elvan.udukkai.localization.language_keys.taLatn[com.elvan.udukkai.localization.K.company]
        kotlin.test.assertNotNull(niruvanam)
        assertEquals("Niruvanam", niruvanam)
    }
}
