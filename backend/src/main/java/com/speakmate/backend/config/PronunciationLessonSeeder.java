package com.speakmate.backend.config;

import com.speakmate.backend.model.entity.Lesson;
import com.speakmate.backend.repository.LessonRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
public class PronunciationLessonSeeder {

    private static String audioBtn(String text) {
        return "<button onclick='playAudio(`" + text + "`)' class='inline-flex items-center justify-center w-6 h-6 rounded-full bg-primary/10 text-primary hover:bg-primary/20 transition-all ml-1 mr-1 align-middle' title='Listen'><span class='material-symbols-outlined text-[14px]'>volume_up</span></button>";
    }

    private static String micBtn(String text) {
        return "<button onclick='startPractice(`" + text + "`)' class='inline-flex items-center justify-center w-6 h-6 rounded-full bg-secondary/10 text-secondary hover:bg-secondary/20 transition-all ml-1 mr-1 align-middle' title='Speak & Practice'><span class='material-symbols-outlined text-[14px]'>mic</span></button>";
    }

    private static String speak(String text) {
        return text + " " + audioBtn(text) + micBtn(text);
    }

    private static String sound(String symbol, String textName) {
        return symbol + " " + audioBtn(textName);
    }

    public static void seed(LessonRepository lessonRepository, String thSoundContent) {
        log.info("Starting safe seeding and updates of Pronunciation lessons...");
        int orderIndex = 1;

        // 1. Introduction
        saveOrUpdate(lessonRepository, "Introduction", "What is Pronunciation", orderIndex++, getWhatIsPronunciationJson());
        saveOrUpdate(lessonRepository, "Introduction", "IPA Basics", orderIndex++, getIpaBasicsJson());
        saveOrUpdate(lessonRepository, "Introduction", "Self Assessment", orderIndex++, getSelfAssessmentJson());

        // 2. Vowel Sounds (12 Pure Vowels)
        saveOrUpdate(lessonRepository, "Vowel Sounds (12 Pure Vowels)", "Short Vowels: /ɪ/ /e/ /æ/ /ʌ/ /ɒ/ /ʊ/ /ə/", orderIndex++, getShortVowelsJson());
        saveOrUpdate(lessonRepository, "Vowel Sounds (12 Pure Vowels)", "Long Vowels: /iː/ /ɑː/ /ɔː/ /uː/ /ɜː/", orderIndex++, getLongVowelsJson());

        // 3. Diphthongs (8 Sounds)
        saveOrUpdate(lessonRepository, "Diphthongs (8 Sounds)", "/eɪ/ /aɪ/ /ɔɪ/ /əʊ/ /aʊ/ /ɪə/ /eə/ /ʊə/", orderIndex++, getDiphthongsJson());

        // 4. Consonant Sounds
        saveOrUpdate(lessonRepository, "Consonant Sounds", "Plosives (p/b, t/d, k/g)", orderIndex++, getPlosivesJson());
        saveOrUpdate(lessonRepository, "Consonant Sounds", "Fricatives (f/v, s/z, ʃ/ʒ, h)", orderIndex++, getFricativesJson());
        saveOrUpdate(lessonRepository, "Consonant Sounds", "TH Sound (Voiced & Unvoiced)", orderIndex++, thSoundContent);
        saveOrUpdate(lessonRepository, "Consonant Sounds", "L vs R", orderIndex++, getLvsRJson());
        saveOrUpdate(lessonRepository, "Consonant Sounds", "V vs W", orderIndex++, getVvsWJson());
        saveOrUpdate(lessonRepository, "Consonant Sounds", "P vs F, S vs SH, Other Consonant Pairs", orderIndex++, getOtherConsonantPairsJson());

        // 5. Minimal Pairs
        saveOrUpdate(lessonRepository, "Minimal Pairs", "Ship vs Sheep, Bit vs Beat, Cat vs Cut, Right vs Light, Think vs Sink", orderIndex++, getMinimalPairsJson());

        // 6. Word Stress
        saveOrUpdate(lessonRepository, "Word Stress", "Syllables & Stress Rules", orderIndex++, getSyllablesStressRulesJson());
        saveOrUpdate(lessonRepository, "Word Stress", "Noun vs Verb Stress Pairs", orderIndex++, getNounVerbStressPairsJson());

        // 7. Sentence Stress & Intonation
        saveOrUpdate(lessonRepository, "Sentence Stress & Intonation", "Content vs Function Words", orderIndex++, getContentFunctionWordsJson());
        saveOrUpdate(lessonRepository, "Sentence Stress & Intonation", "Rising, Falling, Rise-Fall Intonation", orderIndex++, getIntonationJson());

        // 8. Connected Speech
        saveOrUpdate(lessonRepository, "Connected Speech", "Linking, Elision, Assimilation, Weak Forms", orderIndex++, getConnectedSpeechJson());

        // 9. Indian English Corrections
        saveOrUpdate(lessonRepository, "Indian English Corrections", "V vs W Confusion", orderIndex++, getVvsWConfusionJson());
        saveOrUpdate(lessonRepository, "Indian English Corrections", "TH Sound Missing", orderIndex++, getThSoundMissingJson());
        saveOrUpdate(lessonRepository, "Indian English Corrections", "P vs F Confusion", orderIndex++, getPvsFConfusionJson());
        saveOrUpdate(lessonRepository, "Indian English Corrections", "Tamil Speaker Specific Corrections", orderIndex++, getTamilSpeakerCorrectionsJson());

        // 10. Word Pronunciation Guides
        saveOrUpdate(lessonRepository, "Word Pronunciation Guides", "100 Most Mispronounced Words", orderIndex++, getMispronouncedWordsJson());

        // 11. Tongue Twisters
        saveOrUpdate(lessonRepository, "Tongue Twisters", "Beginner", orderIndex++, getBeginnerTwistersJson());
        saveOrUpdate(lessonRepository, "Tongue Twisters", "Intermediate", orderIndex++, getIntermediateTwistersJson());
        saveOrUpdate(lessonRepository, "Tongue Twisters", "Advanced", orderIndex++, getAdvancedTwistersJson());

        log.info("Pronunciation lesson seeding and updates completed successfully.");
    }

    private static void saveOrUpdate(LessonRepository repo, String section, String title, int orderIndex, String jsonContent) {
        Optional<Lesson> opt = repo.findByModuleAndTitle("PRONUNCIATION", title);
        if (opt.isPresent()) {
            Lesson l = opt.get();
            l.setSection(section);
            l.setOrderIndex(orderIndex);
            if (l.getContent().contains("/placeholder/") || !l.getContent().equals(jsonContent)) {
                l.setContent(jsonContent);
                repo.save(l);
            }
        } else {
            repo.save(Lesson.builder()
                    .module("PRONUNCIATION")
                    .section(section)
                    .title(title)
                    .orderIndex(orderIndex)
                    .content(jsonContent)
                    .build());
        }
    }

    // --- JSON Contents ---

    private static String getWhatIsPronunciationJson() {
        return """
        {
          "title": "What is Pronunciation",
          "phoneme": "/prəˌnʌn.siˈeɪ.ʃən/",
          "description": "How we produce the sounds of a language to be clearly understood.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### What is Pronunciation?\\n\\nPronunciation is how we produce the sounds of a language. Good pronunciation isn't about sounding like a native speaker — it's about being clearly understood.\\n\\nFor Indian English speakers, certain sounds (like %s or %s in 'think'/'that') and stress patterns differ from mother-tongue influence, and this course will systematically address these areas.\\n\\nTry practicing these sounds now:\\n*   %s\\n*   %s\\n*   %s\\n*   %s\\n*   %s\\n*   %s\\n*   %s",
          "words": [
            {"word": "pronounce", "ipa": "/prəˈnaʊns/", "meaning": "To make the sound of a word or letter"},
            {"word": "understand", "ipa": "/ˌʌn.dəˈstænd/", "meaning": "To perceive the intended meaning of"},
            {"word": "clear", "ipa": "/klɪər/", "meaning": "Easy to perceive, understand, or interpret"}
          ],
          "target_phrase": "Good pronunciation is about being clearly understood.",
          "mini_quiz": [
            {
              "question": "What is the primary goal of learning good pronunciation?",
              "options": ["To sound exactly like a British native speaker", "To be clearly understood by listeners", "To write faster in exams", "To eliminate all accent variation entirely"],
              "answer": "To be clearly understood by listeners"
            }
          ]
        }
        """.formatted(
            sound("/θ/", "th"),
            sound("/ð/", "th"),
            speak("think"),
            speak("that"),
            speak("very"),
            speak("wet"),
            speak("lorry"),
            speak("light"),
            speak("right")
        );
    }

    private static String getIpaBasicsJson() {
        return """
        {
          "title": "IPA Basics",
          "phoneme": "/aɪ.piː.eɪ/",
          "description": "International Phonetic Alphabet - a consistent way to represent speech sounds.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### What is IPA?\\n\\n**IPA** (International Phonetic Alphabet) is a standardized set of symbols representing every distinct sound in spoken language.\\n\\nUnlike English spelling (which is inconsistent — 'through', 'though', 'tough' all have different 'ough' sounds), IPA symbols always represent the exact same sound.\\n\\n*   Example: %s always represents the 'th' sound in %s, regardless of spelling.\\n*   Example: %s always represents the long 'ee' sound in %s.",
          "words": [
            {"word": "alphabet", "ipa": "/ˈæl.fə.bet/", "meaning": "A set of letters or symbols in a fixed order used to write a language"},
            {"word": "symbol", "ipa": "/ˈsɪm.bəl/", "meaning": "A mark or character used as a conventional representation of something"},
            {"word": "consistent", "ipa": "/kənˈsɪs.tənt/", "meaning": "Consistently adhering to the same principles; stable"}
          ],
          "target_phrase": "IPA represents sounds consistently.",
          "mini_quiz": [
            {
              "question": "Why is the International Phonetic Alphabet useful?",
              "options": ["It translates English words into other foreign languages", "It represents sounds consistently, unlike inconsistent English spelling", "It is only used by actors and singers", "It changes every year to adapt to modern slang"],
              "answer": "It represents sounds consistently, unlike inconsistent English spelling"
            }
          ]
        }
        """.formatted(
            sound("/θ/", "th"),
            speak("think"),
            sound("/iː/", "ee"),
            speak("sheep")
        );
    }

    private static String getSelfAssessmentJson() {
        return """
        {
          "title": "Self Assessment",
          "phoneme": "/əˈses.mənt/",
          "description": "Assess your own pronunciation to find target areas of improvement.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Self Assessment\\n\\nTry practicing these pairs to find your focus areas:\\n\\n1.  Say %s and %s — can you feel the difference in tongue position? (For 'think', tongue tip goes between teeth).\\n2.  Say %s and %s — do you pronounce V and W differently? V uses teeth on lip; W uses rounded lips.\\n3.  Say %s — is the stress on PHO-to-graph or pho-TO-graph? (Correct: PHO-to-graph).",
          "words": [
            {"word": "assessment", "ipa": "/əˈses.mənt/", "meaning": "The evaluation or estimation of the nature, quality, or ability of someone"},
            {"word": "focus", "ipa": "/ˈfəʊ.kəs/", "meaning": "The center of interest or activity"},
            {"word": "practice", "ipa": "/ˈpræk.tɪs/", "meaning": "Perform an activity or exercise repeatedly or regularly"}
          ],
          "target_phrase": "I think this is very interesting.",
          "mini_quiz": [
            {
              "question": "Which syllable should be stressed in the word 'photograph'?",
              "options": ["First syllable (PHO-to-graph)", "Second syllable (pho-TO-graph)", "Third syllable (pho-to-GRAPH)", "All syllables equally stressed"],
              "answer": "First syllable (PHO-to-graph)"
            }
          ]
        }
        """.formatted(
            speak("think"),
            speak("sink"),
            speak("very"),
            speak("wary"),
            speak("photograph")
        );
    }

    private static String getShortVowelsJson() {
        return """
        {
          "title": "Short Vowels: /ɪ/ /e/ /æ/ /ʌ/ /ɒ/ /ʊ/ /ə/",
          "phoneme": "Short Vowels",
          "description": "English has 7 short vowel sounds that carry the core sound of unstressed or fast syllables.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### The 7 Short Vowel Sounds\\n\\nEnglish has 12 pure vowel sounds (monophthongs) — 7 typically classified as 'short' and 5 as 'long'.\\n\\nPractice the 7 Short Vowels below:\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s (schwa) as in: %s, %s, %s (appears in unstressed syllables).\\n\\n### Common Mistakes (Indian English)\\nWrong: pronouncing 'ship' and 'sheep' identically. Correct: 'ship' uses short %s, 'sheep' uses long %s.",
          "words": [
            {"word": "ship", "ipa": "/ʃɪp/", "meaning": "A large boat for transporting goods or people by sea"},
            {"word": "bed", "ipa": "/bed/", "meaning": "A piece of furniture for sleep or rest"},
            {"word": "about", "ipa": "/əˈbaʊt/", "meaning": "On the subject of; concerning"}
          ],
          "target_phrase": "This ship is carrying a cup of hot soup.",
          "minimal_pairs_listen": [
            {"word1": "ship", "word2": "sheep", "correct": "ship", "ipa1": "/ʃɪp/", "ipa2": "/ʃiːp/"},
            {"word1": "bit", "word2": "beat", "correct": "bit", "ipa1": "/bɪt/", "ipa2": "/biːt/"},
            {"word1": "full", "word2": "fool", "correct": "full", "ipa1": "/fʊl/", "ipa2": "/fuːl/"}
          ],
          "mini_quiz": [
            {
              "question": "Which short vowel sound is the most common sound in English and appears in unstressed syllables?",
              "options": ["/ɪ/", "/æ/", "/ə/ (schwa)", "/ɒ/"],
              "answer": "/ə/ (schwa)"
            }
          ]
        }
        """.formatted(
            sound("/ɪ/", "it"), speak("sit"), speak("bit"), speak("hit"), speak("ship"),
            sound("/e/", "et"), speak("bed"), speak("red"), speak("set"), speak("ten"),
            sound("/æ/", "at"), speak("cat"), speak("bat"), speak("man"), speak("hat"),
            sound("/ʌ/", "up"), speak("cup"), speak("bus"), speak("mud"), speak("luck"),
            sound("/ɒ/", "ot"), speak("hot"), speak("pot"), speak("dog"), speak("box"),
            sound("/ʊ/", "oot"), speak("book"), speak("foot"), speak("good"), speak("wood"),
            sound("/ə/", "schwa"), speak("about"), speak("sofa"), speak("banana"),
            sound("/ɪ/", "ship"), sound("/iː/", "sheep")
        );
    }

    private static String getLongVowelsJson() {
        return """
        {
          "title": "Long Vowels: /iː/ /ɑː/ /ɔː/ /uː/ /ɜː/",
          "phoneme": "Long Vowels",
          "description": "English has 5 long vowel sounds that should be held noticeably longer.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### The 5 Long Vowel Sounds\\n\\nLong vowel sounds require you to hold the sound longer than short vowels. Contrast is key for meaning!\\n\\nPractice the 5 Long Vowels below:\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n\\n### Common Mistakes\\nWrong: shortening long vowels, making 'seat' sound like 'sit'. Hold long vowels noticeably longer than short vowels.",
          "words": [
            {"word": "sheep", "ipa": "/ʃiːp/", "meaning": "A domesticated ruminant animal with a thick woolly coat"},
            {"word": "food", "ipa": "/fuːd/", "meaning": "Any nutritious substance that people eat or drink"},
            {"word": "bird", "ipa": "/bɜːd/", "meaning": "A feathered, winged, warm-blooded vertebrate animal"}
          ],
          "target_phrase": "The blue bird flies far over the green tree.",
          "minimal_pairs_listen": [
            {"word1": "sit", "word2": "seat", "correct": "seat", "ipa1": "/sɪt/", "ipa2": "/siːt/"},
            {"word1": "cut", "word2": "cart", "correct": "cart", "ipa1": "/kʌt/", "ipa2": "/kɑːt/"},
            {"word1": "fool", "word2": "full", "correct": "fool", "ipa1": "/fuːl/", "ipa2": "/fʊl/"}
          ],
          "mini_quiz": [
            {
              "question": "Which vowel sound is present in the word 'food'?",
              "options": ["/ʊ/", "/uː/", "/ɔː/", "/ʌ/"],
              "answer": "/uː/"
            }
          ]
        }
        """.formatted(
            sound("/iː/", "ee"), speak("see"), speak("tree"), speak("feet"), speak("need"),
            sound("/ɑː/", "ah"), speak("car"), speak("far"), speak("star"), speak("arm"),
            sound("/ɔː/", "or"), speak("law"), speak("saw"), speak("floor"), speak("more"),
            sound("/uː/", "oo"), speak("food"), speak("moon"), speak("blue"), speak("choose"),
            sound("/ɜː/", "er"), speak("bird"), speak("word"), speak("her"), speak("learn")
        );
    }

    private static String getDiphthongsJson() {
        return """
        {
          "title": "/eɪ/ /aɪ/ /ɔɪ/ /əʊ/ /aʊ/ /ɪə/ /eə/ /ʊə/",
          "phoneme": "Diphthongs",
          "description": "Glides formed by combining two vowel sounds within the same syllable.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### The 8 Diphthong Sounds\\n\\nA diphthong is a sound formed by combining two vowel sounds within the same syllable, gliding smoothly from one to the other.\\n\\nPractice the 8 Diphthongs below:\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n*   %s as in: %s, %s, %s, %s\\n\\n### Common Mistakes\\nWrong: pronouncing diphthongs as flat single vowels (e.g. 'day' sounding like 'deh' or 'go' sounding like 'goh'). Ensure a smooth glide.",
          "words": [
            {"word": "boy", "ipa": "/bɔɪ/", "meaning": "A male child or young man"},
            {"word": "here", "ipa": "/hɪər/", "meaning": "In, at, or to this place or position"},
            {"word": "now", "ipa": "/naʊ/", "meaning": "At the present time or moment"}
          ],
          "target_phrase": "I go home now.",
          "mini_quiz": [
            {
              "question": "Which diphthong is present in the word 'here'?",
              "options": ["/eɪ/", "/ɪə/", "/eə/", "/ʊə/"],
              "answer": "/ɪə/"
            }
          ]
        }
        """.formatted(
            sound("/eɪ/", "ay"), speak("day"), speak("say"), speak("play"), speak("cake"),
            sound("/aɪ/", "eye"), speak("my"), speak("fly"), speak("night"), speak("time"),
            sound("/ɔɪ/", "oy"), speak("boy"), speak("toy"), speak("coin"), speak("voice"),
            sound("/əʊ/", "oh"), speak("go"), speak("show"), speak("home"), speak("boat"),
            sound("/aʊ/", "ow"), speak("how"), speak("now"), speak("out"), speak("cow"),
            sound("/ɪə/", "eer"), speak("here"), speak("near"), speak("ear"), speak("clear"),
            sound("/eə/", "air"), speak("there"), speak("where"), speak("air"), speak("care"),
            sound("/ʊə/", "oor"), speak("pure"), speak("sure"), speak("tour"), speak("cure")
        );
    }

    private static String getPlosivesJson() {
        return """
        {
          "title": "Plosives (p/b, t/d, k/g)",
          "phoneme": "Plosives",
          "description": "Speech sounds formed by completely stopping the flow of air and then releasing it.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Plosive Sounds\\n\\nPlosives are consonant sounds produced by blocking the air flow in your mouth and then releasing it suddenly with a puff of air (aspiration).\\n\\nPractice the primary plosive pairs below:\\n*   Voiceless %s vs Voiced %s as in: %s vs %s\\n*   Voiceless %s vs Voiced %s as in: %s vs %s\\n*   Voiceless %s vs Voiced %s as in: %s vs %s",
          "words": [
            {"word": "pen", "ipa": "/pen/", "meaning": "An instrument for writing with ink"},
            {"word": "tin", "ipa": "/tɪn/", "meaning": "A silvery-white metal; a container made of tinplate"},
            {"word": "cap", "ipa": "/kæp/", "meaning": "A flat lid or cover; a type of hat"}
          ],
          "target_phrase": "Peter picked a big bag of green beans.",
          "mini_quiz": [
            {
              "question": "Which sound is the voiceless plosive counterpart to /d/?",
              "options": ["/p/", "/t/", "/k/", "/s/"],
              "answer": "/t/"
            }
          ]
        }
        """.formatted(
            sound("/p/", "p"), sound("/b/", "b"), speak("pen"), speak("ben"),
            sound("/t/", "t"), sound("/d/", "d"), speak("tin"), speak("din"),
            sound("/k/", "k"), sound("/ɡ/", "g"), speak("cap"), speak("gap")
        );
    }

    private static String getFricativesJson() {
        return """
        {
          "title": "Fricatives (f/v, s/z, ʃ/ʒ, h)",
          "phoneme": "Fricatives",
          "description": "Sounds made by squeezing air through a narrow gap, producing a continuous friction noise.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Fricative Sounds\\n\\nFricative consonants are produced by forcing air through a narrow channel made by placing two articulators close together (e.g. teeth on lip, tongue near palate).\\n\\nPractice the fricative pairs:\\n*   Voiceless %s vs Voiced %s as in: %s vs %s\\n*   Voiceless %s vs Voiced %s as in: %s vs %s\\n*   Voiceless %s vs Voiced %s as in: %s vs %s\\n*   Glottal %s as in: %s",
          "words": [
            {"word": "fan", "ipa": "/fæn/", "meaning": "A device for creating a current of air"},
            {"word": "van", "ipa": "/væn/", "meaning": "A medium-sized motor vehicle for transporting goods"},
            {"word": "ship", "ipa": "/ʃɪp/", "meaning": "A large vessel sailing on the seas"}
          ],
          "target_phrase": "She sells fresh fish at the busy shop.",
          "mini_quiz": [
            {
              "question": "Which sound is a voiced fricative produced with teeth on lip?",
              "options": ["/f/", "/v/", "/s/", "/h/"],
              "answer": "/v/"
            }
          ]
        }
        """.formatted(
            sound("/f/", "f"), sound("/v/", "v"), speak("fan"), speak("van"),
            sound("/s/", "s"), sound("/z/", "z"), speak("sip"), speak("zip"),
            sound("/ʃ/", "sh"), sound("/ʒ/", "zh"), speak("ship"), speak("measure"),
            sound("/h/", "h"), speak("hot")
        );
    }

    private static String getLvsRJson() {
        return """
        {
          "title": "L vs R",
          "phoneme": "/l/ vs /r/",
          "description": "Distinguishing tongue positions for the lateral /l/ and retroflex /r/ sounds.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### L and R Sound Distinction\\n\\nL and R are frequently confused by Tamil and other Indian language speakers. Here is how to produce them correctly:\\n\\n*   **Sound Production /l/** — tongue tip touches the ridge behind upper front teeth: %s, %s, %s, %s\\n*   **Sound Production /r/** — tongue curls back slightly, does NOT touch the roof of the mouth: %s, %s, %s, %s\\n\\n### Common Mistakes\\nWrong: pronouncing 'right' and 'light' identically. Remember that /r/ requires no contact with the roof of the mouth.",
          "words": [
            {"word": "light", "ipa": "/laɪt/", "meaning": "The natural agent that stimulates sight"},
            {"word": "right", "ipa": "/raɪt/", "meaning": "Morally good, justified, or correct; opposite of left"},
            {"word": "lorry", "ipa": "/ˈlɒr.i/", "meaning": "A large, heavy motor vehicle for transporting goods"}
          ],
          "target_phrase": "The red lorry drives right past the light.",
          "minimal_pairs_listen": [
            {"word1": "light", "word2": "right", "correct": "light", "ipa1": "/laɪt/", "ipa2": "/raɪt/"},
            {"word1": "collect", "word2": "correct", "correct": "correct", "ipa1": "/kəˈlekt/", "ipa2": "/kəˈrekt/"},
            {"word1": "glass", "word2": "grass", "correct": "glass", "ipa1": "/ɡlɑːs/", "ipa2": "/ɡrɑːs/"}
          ],
          "mini_quiz": [
            {
              "question": "Does the tongue touch the roof of the mouth for the correct pronunciation of the /r/ sound?",
              "options": ["Yes, it touches the center.", "No, it curls back slightly without touching.", "Yes, it touches the front teeth.", "Yes, it touches the side molars."],
              "answer": "No, it curls back slightly without touching."
            }
          ]
        }
        """.formatted(
            speak("light"), speak("lake"), speak("pull"), speak("feel"),
            speak("right"), speak("rake"), speak("red"), speak("carry")
        );
    }

    private static String getVvsWJson() {
        return """
        {
          "title": "V vs W",
          "phoneme": "/v/ vs /w/",
          "description": "Comparing teeth-to-lip contact for /v/ with rounded lips for /w/.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### V vs W Pronunciation\\n\\nThis is one of the most common Indian English errors. They use completely different mouth shapes:\\n\\n*   **/v/ Sound Production** — upper teeth touch lower lip (like biting your lip gently), with voice vibration: %s, %s, %s\\n*   **/w/ Sound Production** — lips rounded and pushed forward, NO teeth contact: %s, %s, %s\\n\\n### Common Mistakes\\nWrong: saying 'wery' instead of 'very', or 'vine' instead of 'wine'. Practice feeling teeth-on-lip for V vs rounded lips for W.",
          "words": [
            {"word": "very", "ipa": "/ˈver.i/", "meaning": "In a high degree; extremely"},
            {"word": "wine", "ipa": "/waɪn/", "meaning": "An alcoholic drink made from fermented grape juice"},
            {"word": "vine", "ipa": "/vaɪn/", "meaning": "A climbing woody-stemmed plant of the grape family"}
          ],
          "target_phrase": "We went to visit our very vibrant village.",
          "minimal_pairs_listen": [
            {"word1": "vine", "word2": "wine", "correct": "wine", "ipa1": "/vaɪn/", "ipa2": "/waɪn/"},
            {"word1": "vet", "word2": "wet", "correct": "vet", "ipa1": "/vet/", "ipa2": "/wet/"},
            {"word1": "verse", "word2": "worse", "correct": "verse", "ipa1": "/vɜːs/", "ipa2": "/wɜːs/"}
          ],
          "mini_quiz": [
            {
              "question": "Which sound is produced with your upper front teeth touching your lower lip?",
              "options": ["/w/", "/v/", "/b/", "/p/"],
              "answer": "/v/"
            }
          ]
        }
        """.formatted(
            speak("van"), speak("very"), speak("voice"),
            speak("win"), speak("wine"), speak("word")
        );
    }

    private static String getOtherConsonantPairsJson() {
        return """
        {
          "title": "P vs F, S vs SH, Other Consonant Pairs",
          "phoneme": "/p/ vs /f/ & /s/ vs /ʃ/",
          "description": "Sharpening distinctions for common confusing consonant pairs.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### P vs F & S vs SH\\n\\n### P vs F\\n*   **/p/** — lips fully close then release with a puff of air (aspiration): %s, %s, %s\\n*   **/f/** — upper teeth touch lower lip, air flows continuously: %s, %s, %s\\n\\n### S vs SH\\n*   **/s/** — tongue near roof of mouth, producing a sharp hissing sound: %s, %s, %s\\n*   **/ʃ/** (sh) — tongue slightly further back, softer 'shushing' sound: %s, %s, %s\\n\\n### Common Mistakes\\nWrong: saying 'pone' instead of 'phone' (confusing P and F). Remember that 'phone' starts with an /f/ sound.",
          "words": [
            {"word": "phone", "ipa": "/fəʊn/", "meaning": "A telephone; a device to talk to someone at a distance"},
            {"word": "sheep", "ipa": "/ʃiːp/", "meaning": "A woolly animal"},
            {"word": "sip", "ipa": "/sɪp/", "meaning": "Drink in small mouthfuls"}
          ],
          "target_phrase": "She sells seashells by the seashore.",
          "mini_quiz": [
            {
              "question": "Which of these words starts with the /f/ sound?",
              "options": ["pen", "phone", "pig", "pan"],
              "answer": "phone"
            }
          ]
        }
        """.formatted(
            speak("pen"), speak("pig"), speak("cap"),
            speak("fan"), speak("fig"), speak("cuff"),
            speak("sip"), speak("bus"), speak("sun"),
            speak("ship"), speak("wash"), speak("shoe")
        );
    }

    private static String getMinimalPairsJson() {
        return """
        {
          "title": "Ship vs Sheep, Bit vs Beat, Cat vs Cut, Right vs Light, Think vs Sink",
          "phoneme": "Minimal Pairs",
          "description": "Words that differ by only one sound to test and refine your acoustic discrimination.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Minimal Pairs\\n\\nMinimal pairs are two words that differ by only one sound — practicing these sharpens your ability to hear and produce subtle sound differences.\\n\\nPractice contrasting these pairs:\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s\\n*   %s vs %s",
          "words": [
            {"word": "rice", "ipa": "/raɪs/", "meaning": "A food grain"},
            {"word": "lice", "ipa": "/laɪs/", "meaning": "Plural of louse; tiny parasitic insects"},
            {"word": "think", "ipa": "/θɪŋk/", "meaning": "Have a particular belief or opinion"}
          ],
          "target_phrase": "Minimal pairs show subtle sound differences.",
          "minimal_pairs_listen": [
            {"word1": "ship", "word2": "sheep", "correct": "ship", "ipa1": "/ʃɪp/", "ipa2": "/ʃiːp/"},
            {"word1": "bit", "word2": "beat", "correct": "bit", "ipa1": "/bɪt/", "ipa2": "/biːt/"},
            {"word1": "cat", "word2": "cut", "correct": "cat", "ipa1": "/kæt/", "ipa2": "/kʌt/"},
            {"word1": "pen", "word2": "pin", "correct": "pen", "ipa1": "/pen/", "ipa2": "/pɪn/"},
            {"word1": "right", "word2": "light", "correct": "light", "ipa1": "/raɪt/", "ipa2": "/laɪt/"}
          ],
          "mini_quiz": [
            {
              "question": "What is a minimal pair?",
              "options": ["Two words that are spelled identically but mean different things", "Two words that differ by exactly one sound", "Two words that are synonyms", "Two words from different root languages"],
              "answer": "Two words that differ by exactly one sound"
            }
          ]
        }
        """.formatted(
            speak("ship"), speak("sheep"),
            speak("bit"), speak("beat"),
            speak("cat"), speak("cut"),
            speak("pen"), speak("pin"),
            speak("rice"), speak("lice"),
            speak("road"), speak("load"),
            speak("right"), speak("light"),
            speak("berry"), speak("very"),
            speak("wine"), speak("vine"),
            speak("think"), speak("sink"),
            speak("then"), speak("den"),
            speak("full"), speak("fool"),
            speak("bad"), speak("bed")
        );
    }

    private static String getSyllablesStressRulesJson() {
        return """
        {
          "title": "Syllables & Stress Rules",
          "phoneme": "Syllable Stress",
          "description": "Emphasizing one syllable more than others in a multi-syllable word.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Syllables & Stress Rules\\n\\nWord stress refers to emphasizing one syllable more than others in a multi-syllable word. Incorrect stress placement is a major reason non-native speakers are hard to understand, even when individual sounds are correct.\\n\\nEnglish requires clear stress contrast — one strong syllable, others weaker.\\n\\nPractice these stress shifts:\\n*   %s (stress on PHO-to-graph)\\n*   %s (stress on pho-TOG-ra-pher)\\n*   %s (stress on pho-to-GRAPH-ic)",
          "words": [
            {"word": "computer", "ipa": "/kəmˈpjuː.tər/", "meaning": "An electronic device for storing and processing data"},
            {"word": "photograph", "ipa": "/ˈfəʊ.tə.ɡrɑːf/", "meaning": "A picture made using a camera"},
            {"word": "photographer", "ipa": "/fəˈtɒɡ.rə.fər/", "meaning": "A person who takes photographs"}
          ],
          "target_phrase": "Accent and stress are important for pronunciation.",
          "mini_quiz": [
            {
              "question": "Where should the stress fall in the word 'computer'?",
              "options": ["On the first syllable (COM-puter)", "On the second syllable (com-PU-ter)", "On the third syllable (compu-TER)", "Stressed equally on all syllables"],
              "answer": "On the second syllable (com-PU-ter)"
            }
          ]
        }
        """.formatted(
            speak("photograph"),
            speak("photographer"),
            speak("photographic")
        );
    }

    private static String getNounVerbStressPairsJson() {
        return """
        {
          "title": "Noun vs Verb Stress Pairs",
          "phoneme": "Noun vs Verb Stress",
          "description": "Shifting stress placement depending on whether the word is used as a noun or a verb.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Noun vs Verb Stress Pairs\\n\\nMany two-syllable words in English change stress depending on their grammatical function. Rule of thumb: **Nouns** stress the **first** syllable; **Verbs** stress the **second** syllable.\\n\\nPractice these pairs:\\n*   %s (Noun: PER-mit) vs %s (Verb: per-MIT)\\n*   %s (Noun: RE-cord) vs %s (Verb: re-CORD)\\n*   %s (Noun: PRO-test) vs %s (Verb: pro-TEST)\\n*   %s (Noun: OB-ject) vs %s (Verb: ob-JECT)\\n*   %s (Noun: PRO-duce) vs %s (Verb: pro-DUCE)",
          "words": [
            {"word": "permit", "ipa": "/ˈpɜː.mɪt/", "meaning": "An official document giving permission (Noun)"},
            {"word": "record", "ipa": "/ˈrek.ɔːd/", "meaning": "A thin plastic disc on which music is recorded; history (Noun)"},
            {"word": "protest", "ipa": "/ˈprəʊ.test/", "meaning": "A statement or action expressing disapproval (Noun)"}
          ],
          "target_phrase": "Please record a new record of the protest.",
          "mini_quiz": [
            {
              "question": "Which syllable is stressed when saying 'record' as a verb?",
              "options": ["The first syllable (RE-cord)", "The second syllable (re-CORD)", "Both syllables equally", "Neither syllable is stressed"],
              "answer": "The second syllable (re-CORD)"
            }
          ]
        }
        """.formatted(
            speak("permit"), speak("permit"),
            speak("record"), speak("record"),
            speak("protest"), speak("protest"),
            speak("object"), speak("object"),
            speak("produce"), speak("produce")
        );
    }

    private static String getContentFunctionWordsJson() {
        return """
        {
          "title": "Content vs Function Words",
          "phoneme": "Sentence Stress",
          "description": "Stressing meaning-carrying words while sliding over grammar words.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Content vs Function Words\\n\\nIn connected speech, not all words are stressed equally. Content words (carrying meaning) are stressed; function words (grammar words) are usually unstressed.\\n\\n*   **Content Words (Stressed)**: Nouns, main verbs, adjectives, adverbs, negatives (not, never)\\n*   **Function Words (Unstressed)**: Articles (a, the), prepositions (in, on), pronouns (he, it), auxiliary verbs (is, have)\\n\\nExample: In the sentence 'I %s to %s to the %s', 'want', 'go', and 'market' are stressed; 'to' and 'the' are unstressed and spoken quickly.",
          "words": [
            {"word": "want", "ipa": "/wɒnt/", "meaning": "Have a desire to possess or do something"},
            {"word": "market", "ipa": "/ˈmɑː.kɪt/", "meaning": "A regular gathering of people for the purchase and sale of provisions"},
            {"word": "go", "ipa": "/ɡəʊ/", "meaning": "Move from one place to another"}
          ],
          "target_phrase": "I want to go to the market.",
          "mini_quiz": [
            {
              "question": "Is the article 'the' typically stressed or unstressed in standard English speech?",
              "options": ["Stressed", "Unstressed", "Stressed only in short sentences", "It depends on the dialect"],
              "answer": "Unstressed"
            }
          ]
        }
        """.formatted(
            speak("want"),
            speak("go"),
            speak("market")
        );
    }

    private static String getIntonationJson() {
        return """
        {
          "title": "Rising, Falling, Rise-Fall Intonation",
          "phoneme": "Intonation",
          "description": "Varying pitch to express grammatical structure, questions, and emotion.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Intonation Patterns\\n\\nIntonation is the rise and fall of pitch in the voice when speaking. It gives sentences their emotional context.\\n\\n*   **Rising Intonation ↗** — used for yes/no questions: %s ↗\\n*   **Falling Intonation ↘** — used for statements and Wh- questions: %s ↘ / %s ↘\\n*   **Rise-Fall Intonation ↗↘** — expressing surprise, excitement, or definiteness.",
          "words": [
            {"word": "coming", "ipa": "/ˈkʌm.ɪŋ/", "meaning": "Approaching; arriving"},
            {"word": "going", "ipa": "/ˈɡəʊ.ɪŋ/", "meaning": "Departing; moving on"},
            {"word": "question", "ipa": "/ˈkwes.tʃən/", "meaning": "A sentence worded to elicit information"}
          ],
          "target_phrase": "Is this your book? No, this is my book.",
          "mini_quiz": [
            {
              "question": "What intonation pattern is standard for yes/no questions?",
              "options": ["Falling ↘", "Rising ↗", "Flat and monotone", "Rise-Fall ↗↘"],
              "answer": "Rising ↗"
            }
          ]
        }
        """.formatted(
            speak("Are you coming?"),
            speak("I am coming."),
            speak("Where are you going?")
        );
    }

    private static String getConnectedSpeechJson() {
        return """
        {
          "title": "Linking, Elision, Assimilation, Weak Forms",
          "phoneme": "Connected Speech",
          "description": "How speech sounds merge, change, or disappear in natural connected sentences.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Connected Speech\\n\\nNative speakers don't pronounce words in isolation — sounds link, disappear, or weaken in connected sentences.\\n\\n*   **Linking** (Consonant to Vowel): %s sounds like 'ter-noff'. Vowel to Vowel: %s sounds like 'gowin'.\\n*   **Elision** (Sounds Disappearing): %s → the /t/ disappears, sounding like 'nex day'. 'and' often becomes 'an' in %s.\\n*   **Weak Forms**: 'to' often becomes /tə/ in %s, 'for' often becomes /fə/ in %s.\\n\\n### Common Mistakes\\nOver-pronouncing every single word separately. Native speech relies on these natural transitions.",
          "words": [
            {"word": "linking", "ipa": "/ˈlɪŋ.kɪŋ/", "meaning": "Connecting or joining things together"},
            {"word": "elision", "ipa": "/iˈlɪʒ.ən/", "meaning": "The omission of a sound or syllable when speaking"},
            {"word": "weak", "ipa": "/wiːk/", "meaning": "Lacking intensity; unstressed"}
          ],
          "target_phrase": "What are you going to do?",
          "mini_quiz": [
            {
              "question": "What is 'elision' in connected speech?",
              "options": ["Stressing the wrong syllable", "The complete disappearance of a sound (e.g. 'next day' -> 'nex day')", "Adding an extra vowel sound between consonants", "Speaking in a monotone pitch"],
              "answer": "The complete disappearance of a sound (e.g. 'next day' -> 'nex day')"
            }
          ]
        }
        """.formatted(
            speak("turn off"),
            speak("go in"),
            speak("next day"),
            speak("fish and chips"),
            speak("I want to go"),
            speak("This is for you")
        );
    }

    private static String getVvsWConfusionJson() {
        return """
        {
          "title": "V vs W Confusion",
          "phoneme": "/v/ vs /w/",
          "description": "Direct correction of the V/W merger common in Indian English.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### V vs W Confusion\\n\\nIn Indian English, /v/ and /w/ are frequently merged. To correct this:\\n*   Make sure you bite your lower lip gently with your upper front teeth for: %s, %s, %s\\n*   Make sure you round your lips fully like a circle and avoid teeth contact for: %s, %s, %s, %s",
          "words": [
            {"word": "visit", "ipa": "/ˈvɪz.ɪt/", "meaning": "Go to see and spend time with"},
            {"word": "went", "ipa": "/went/", "meaning": "Past tense of go"},
            {"word": "word", "ipa": "/wɜːd/", "meaning": "A single distinct meaningful element of speech or writing"}
          ],
          "target_phrase": "We went to visit our very vibrant village.",
          "mini_quiz": [
            {
              "question": "Which word is pronounced with fully rounded lips and no teeth contact?",
              "options": ["very", "went", "visit", "voice"],
              "answer": "went"
            }
          ]
        }
        """.formatted(
            speak("very"), speak("voice"), speak("visit"),
            speak("we"), speak("went"), speak("wine"), speak("word")
        );
    }

    private static String getThSoundMissingJson() {
        return """
        {
          "title": "TH Sound Missing",
          "phoneme": "/θ/ & /ð/",
          "description": "Addressing the replacement of TH with T or D in Indian accents.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Missing TH Sound\\n\\nA common pronunciation feature in Indian English is replacing the dental fricatives /θ/ and /ð/ with retroflex/dental stops /t/ or /d/.\\n\\n*   Wrong: saying 'dis' instead of 'this', or 'tink' instead of 'think'.\\n*   Correction: Your tongue tip **must** go directly between your teeth, and air must flow continuously.\\n\\nPractice these sounds:\\n*   %s\\n*   %s\\n*   %s\\n*   %s\\n*   %s",
          "words": [
            {"word": "think", "ipa": "/θɪŋk/", "meaning": "Have a particular belief or opinion"},
            {"word": "this", "ipa": "/ðɪs/", "meaning": "Used to identify a specific person or thing close at hand"},
            {"word": "thousand", "ipa": "/ˈθaʊ.zənd/", "meaning": "The number 1,000"}
          ],
          "target_phrase": "This thing is worth three thousand.",
          "mini_quiz": [
            {
              "question": "Where must your tongue tip be positioned to pronounce the TH sound (/θ/) correctly?",
              "options": ["Touching the roof of the mouth", "Cooled back without contact", "Directly between your upper and lower teeth", "Pressed against the bottom front teeth"],
              "answer": "Directly between your upper and lower teeth"
            }
          ]
        }
        """.formatted(
            speak("this"),
            speak("that"),
            speak("think"),
            speak("three"),
            speak("thousand")
        );
    }

    private static String getPvsFConfusionJson() {
        return """
        {
          "title": "P vs F Confusion",
          "phoneme": "/p/ vs /f/",
          "description": "Correcting the substitution of P for F (like 'pone' for 'phone') common in some regional dialects.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### P vs F Confusion\\n\\nSome regional Indian language speakers substitute /p/ (plosive) for /f/ (fricative), causing 'phone' to sound like 'pone'.\\n\\n*   **/p/ sound**: Lips fully close, blocking air, then release with a puff: %s, %s\\n*   **/f/ sound**: Upper teeth touch lower lip, air escapes continuously: %s, %s\\n\\nPractice contrasting these pairs:\\n*   %s vs %s\\n*   %s vs %s",
          "words": [
            {"word": "phone", "ipa": "/fəʊn/", "meaning": "A telephone"},
            {"word": "pick", "ipa": "/pɪk/", "meaning": "Take or choose from a selection"},
            {"word": "full", "ipa": "/fʊl/", "meaning": "Containing or holding as much as possible"}
          ],
          "target_phrase": "Please pick up the phone.",
          "mini_quiz": [
            {
              "question": "Which of these words starts with a /p/ sound rather than an /f/ sound?",
              "options": ["phone", "photo", "pick", "full"],
              "answer": "pick"
            }
          ]
        }
        """.formatted(
            speak("pen"), speak("pick"),
            speak("phone"), speak("full"),
            speak("pen"), speak("fan"),
            speak("pull"), speak("full")
        );
    }

    private static String getTamilSpeakerCorrectionsJson() {
        return """
        {
          "title": "Tamil Speaker Specific Corrections",
          "phoneme": "Tamil Accent Fixes",
          "description": "Specific guidance addressing retroflex sounds, final consonants, and vowel insertion (film/fillam).",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Tamil Speaker Specific Corrections\\n\\nTamil speakers often have distinct pronunciation tendencies due to difference in language structures:\\n\\n1.  **Adding Extra Schwa /ə/ Vowels**: Tamil does not have certain consonant clusters, leading speakers to insert extra vowels. E.g., %s becomes 'fillam', or %s becomes 'problam'. Correction: Practice saying the consonant cluster directly without adding any extra vowel sound in between.\\n2.  **Not Dropping Final Consonants**: Ensure final sounds like -t, -d, -k are pronounced clearly, e.g., in %s, %s.\\n3.  **Stress on Wrong Syllables**: Words like %s are sometimes stressed on the first syllable in Indian English. Keep stress on the second syllable: de-DUCT.",
          "words": [
            {"word": "film", "ipa": "/fɪlm/", "meaning": "A movie or motion picture"},
            {"word": "problem", "ipa": "/ˈprɒb.ləm/", "meaning": "A matter or situation regarded as unwelcome or harmful"},
            {"word": "deduct", "ipa": "/dɪˈdʌkt/", "meaning": "Subtract or take away from a total"}
          ],
          "target_phrase": "I have no problem watching that film.",
          "mini_quiz": [
            {
              "question": "What is the common error referred to as 'adding an extra schwa' in consonant clusters?",
              "options": ["Pronouncing 'film' as 'fillam'", "Stressing the wrong syllable in 'deduct'", "Dropping the final consonant in 'tact'", "Replacing the TH sound with a D sound"],
              "answer": "Pronouncing 'film' as 'fillam'"
            }
          ]
        }
        """.formatted(
            speak("film"),
            speak("problem"),
            speak("tact"),
            speak("act"),
            speak("deduct")
        );
    }

    private static String getMispronouncedWordsJson() {
        return """
        {
          "title": "100 Most Mispronounced Words",
          "phoneme": "Mispronounced Words",
          "description": "Practicing commonly mispronounced words, silent letters, and stress shifts.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Commonly Mispronounced Words\\n\\nHere are some of the most common words that trip up English learners due to silent letters or unexpected stress:\\n\\n*   **Colonel** — pronounced %s (not co-lo-nel)\\n*   **Wednesday** — pronounced %s (the first 'd' is silent)\\n*   **February** — pronounced %s or %s\\n*   **Comfortable** — pronounced %s (not all syllables are separate)\\n*   **Vegetable** — pronounced %s\\n*   **Particularly** — pronounced %s\\n*   **Entrepreneur** — pronounced %s\\n*   **Often** — pronounced %s (the 't' is silent)\\n*   **Salmon** — pronounced %s (the 'l' is silent)\\n*   **Receipt** — pronounced %s (the 'p' is silent)\\n\\nPractice each word slowly focusing on correct syllable stress.",
          "words": [
            {"word": "colonel", "ipa": "/ˈkɜː.nəl/", "meaning": "An army officer of high rank"},
            {"word": "receipt", "ipa": "/rɪˈsiːt/", "meaning": "A written statement that money or goods have been received"},
            {"word": "often", "ipa": "/ˈɒf.ən/", "meaning": "Frequently; many times"}
          ],
          "target_phrase": "The entrepreneur is particularly comfortable.",
          "mini_quiz": [
            {
              "question": "Which letter is silent in the word 'receipt'?",
              "options": ["r", "p", "c", "t"],
              "answer": "p"
            }
          ]
        }
        """.formatted(
            speak("kernel"),
            speak("Wenzday"),
            speak("Febroo-ary"),
            speak("Febyoo-ary"),
            speak("Kumfter-bul"),
            speak("Vej-tuh-bul"),
            speak("par-TIK-yuh-lar-lee"),
            speak("on-truh-pruh-NUR"),
            speak("Offen"),
            speak("Salmon"),
            speak("ri-SEET")
        );
    }

    private static String getBeginnerTwistersJson() {
        return """
        {
          "title": "Beginner",
          "phoneme": "Beginner Twisters",
          "description": "Fun, simple tongue twisters to build muscle flexibility for S/SH and L/R.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Beginner Tongue Twisters\\n\\nPractice these simple tongue twisters slowly at first to train your mouth muscles, then gradually increase your speed:\\n\\n*   %s (S/SH practice)\\n*   %s (L/R practice)",
          "words": [
            {"word": "seashells", "ipa": "/ˈsiː.ʃelz/", "meaning": "The shell of a marine mollusk"},
            {"word": "seashore", "ipa": "/ˈsiː.ʃɔːr/", "meaning": "The sandy area along the edge of an ocean"}
          ],
          "target_phrase": "She sells seashells by the seashore.",
          "tongue_twister": {
            "text": "She sells seashells by the seashore."
          },
          "mini_quiz": [
            {
              "question": "Which consonant contrast does the tongue twister 'She sells seashells' help practice?",
              "options": ["V vs W", "L vs R", "S vs SH", "P vs F"],
              "answer": "S vs SH"
            }
          ]
        }
        """.formatted(
            speak("She sells seashells by the seashore."),
            speak("Red lorry, yellow lorry.")
        );
    }

    private static String getIntermediateTwistersJson() {
        return """
        {
          "title": "Intermediate",
          "phoneme": "Intermediate Twisters",
          "description": "Intermediate tongue twisters targeting P and W sounds.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Intermediate Tongue Twisters\\n\\nChallenge your mouth speed with these plosive and rounded lip exercises:\\n\\n*   %s (P plosive practice)\\n*   %s (W rounded lip practice)",
          "words": [
            {"word": "pickled", "ipa": "/ˈpɪk.əld/", "meaning": "Preserved in vinegar or brine"},
            {"word": "woodchuck", "ipa": "/ˈwʊd.tʃʌk/", "meaning": "A marmot of northeastern North America"}
          ],
          "target_phrase": "Peter Piper picked a peck of pickled peppers.",
          "tongue_twister": {
            "text": "Peter Piper picked a peck of pickled peppers."
          },
          "mini_quiz": [
            {
              "question": "Which sound is primarily exercised in the 'Peter Piper' tongue twister?",
              "options": ["/f/ sound", "/p/ sound", "/v/ sound", "/s/ sound"],
              "answer": "/p/ sound"
            }
          ]
        }
        """.formatted(
            speak("Peter Piper picked a peck of pickled peppers."),
            speak("How much wood would a woodchuck chuck if a woodchuck could chuck wood?")
        );
    }

    private static String getAdvancedTwistersJson() {
        return """
        {
          "title": "Advanced",
          "phoneme": "Advanced Twisters",
          "description": "High difficulty tongue twisters exercising TH, S, SH, and K sounds in sequence.",
          "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
          "intro": "### Advanced Tongue Twisters\\n\\nThese are highly challenging consonant cluster exercises. Focus on accuracy over speed first!\\n\\n*   %s (TH /θ/ and /ð/ practice)\\n*   %s (S, SH, and K practice)",
          "words": [
            {"word": "worth", "ipa": "/wɜːθ/", "meaning": "Having a value of; equivalent to"},
            {"word": "sheikh", "ipa": "/ʃeɪk/", "meaning": "An Arab leader or head of a tribe/family"}
          ],
          "target_phrase": "I think that this thing is worth three thousand rupees.",
          "tongue_twister": {
            "text": "The sixth sick sheikh's sixth sheep's sick."
          },
          "mini_quiz": [
            {
              "question": "Which sound contrast is trained in the 'thousand rupees' twister?",
              "options": ["V vs W", "TH voiced and unvoiced", "L vs R", "P vs F"],
              "answer": "TH voiced and unvoiced"
            }
          ]
        }
        """.formatted(
            speak("I think that this thing is worth three thousand rupees."),
            speak("The sixth sick sheikh's sixth sheep's sick.")
        );
    }
}
