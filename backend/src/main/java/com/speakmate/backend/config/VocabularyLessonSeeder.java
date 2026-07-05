package com.speakmate.backend.config;

import com.speakmate.backend.model.entity.Lesson;
import com.speakmate.backend.repository.LessonRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
public class VocabularyLessonSeeder {

    public static void seed(
            LessonRepository lessonRepository,
            String getActiveContent,
            String makePhrasalContent,
            String takePhrasalContent,
            String comePhrasalContent,
            String goPhrasalContent
    ) {
        log.info("Starting safe seeding and updates of Vocabulary lessons...");
        int orderIndex = 1;

        // 1. Introduction
        saveOrUpdate(lessonRepository, "Introduction", "What is Vocabulary", orderIndex++, getWhatIsVocabularyJson());
        saveOrUpdate(lessonRepository, "Introduction", "Why Vocabulary Matters", orderIndex++, getWhyVocabularyMattersJson());
        saveOrUpdate(lessonRepository, "Introduction", "Vocabulary Learning Strategies", orderIndex++, getVocabularyLearningStrategiesJson());

        // 2. Word Types & Forms
        saveOrUpdate(lessonRepository, "Word Types & Forms", "Noun Forms", orderIndex++, getNounFormsJson());
        saveOrUpdate(lessonRepository, "Word Types & Forms", "Verb Forms", orderIndex++, getVerbFormsJson());
        saveOrUpdate(lessonRepository, "Word Types & Forms", "Adjective Forms", orderIndex++, getAdjectiveFormsJson());
        saveOrUpdate(lessonRepository, "Word Types & Forms", "Adverb Forms", orderIndex++, getAdverbFormsJson());
        saveOrUpdate(lessonRepository, "Word Types & Forms", "Word Families", orderIndex++, getWordFamiliesJson());

        // 3. Daily English Vocabulary
        saveOrUpdate(lessonRepository, "Daily English Vocabulary", "Everyday Objects & People", orderIndex++, getEverydayObjectsPeopleJson());
        saveOrUpdate(lessonRepository, "Daily English Vocabulary", "Body, Health & Food", orderIndex++, getBodyHealthFoodJson());
        saveOrUpdate(lessonRepository, "Daily English Vocabulary", "Home, Work & Travel", orderIndex++, getHomeWorkTravelJson());
        saveOrUpdate(lessonRepository, "Daily English Vocabulary", "Nature, Time & Feelings", orderIndex++, getNatureTimeFeelingsJson());

        // 4. Level-wise Vocabulary
        saveOrUpdate(lessonRepository, "Level-wise Vocabulary", "Beginner (A1) Words", orderIndex++, getBeginnerA1Json());
        saveOrUpdate(lessonRepository, "Level-wise Vocabulary", "Elementary (A2) Words", orderIndex++, getElementaryA2Json());
        saveOrUpdate(lessonRepository, "Level-wise Vocabulary", "Intermediate (B1) Words", orderIndex++, getIntermediateB1Json());
        saveOrUpdate(lessonRepository, "Level-wise Vocabulary", "Advanced (B2/C1) Words", orderIndex++, getAdvancedB2C1Json());

        // 5. Synonyms & Antonyms
        saveOrUpdate(lessonRepository, "Synonyms & Antonyms", "Common Synonym Pairs", orderIndex++, getCommonSynonymPairsJson());
        saveOrUpdate(lessonRepository, "Synonyms & Antonyms", "Common Antonym Pairs", orderIndex++, getCommonAntonymPairsJson());
        saveOrUpdate(lessonRepository, "Synonyms & Antonyms", "Synonym Precision (formal vs informal)", orderIndex++, getSynonymPrecisionJson());

        // 6. Phrasal Verbs
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with GET", orderIndex++, getActiveContent);
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with MAKE", orderIndex++, makePhrasalContent);
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with TAKE", orderIndex++, takePhrasalContent);
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with COME", orderIndex++, comePhrasalContent);
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with GO", orderIndex++, goPhrasalContent);

        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with LOOK", orderIndex++, getLookPhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with PUT", orderIndex++, getPutPhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with TURN", orderIndex++, getTurnPhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with BREAK", orderIndex++, getBreakPhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with GIVE", orderIndex++, getGivePhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with SET", orderIndex++, getSetPhrasalJson());
        saveOrUpdate(lessonRepository, "Phrasal Verbs", "Phrasal Verbs with RUN", orderIndex++, getRunPhrasalJson());

        // 7. Idioms & Expressions
        saveOrUpdate(lessonRepository, "Idioms & Expressions", "Body Part Idioms", orderIndex++, getBodyPartIdiomsJson());
        saveOrUpdate(lessonRepository, "Idioms & Expressions", "Animal Idioms", orderIndex++, getAnimalIdiomsJson());
        saveOrUpdate(lessonRepository, "Idioms & Expressions", "Color Idioms", orderIndex++, getColorIdiomsJson());
        saveOrUpdate(lessonRepository, "Idioms & Expressions", "Time Idioms", orderIndex++, getTimeIdiomsJson());
        saveOrUpdate(lessonRepository, "Idioms & Expressions", "Work & Success Idioms", orderIndex++, getWorkSuccessIdiomsJson());

        // 8. Confusing Words
        saveOrUpdate(lessonRepository, "Confusing Words", "Since vs For, Much vs Many, Make vs Do, Say vs Tell, Affect vs Effect, and other commonly confused pairs", orderIndex++, getConfusingWordsJson());

        // 9. Collocations
        saveOrUpdate(lessonRepository, "Collocations", "Verb + Noun Collocations", orderIndex++, getVerbNounCollocationsJson());
        saveOrUpdate(lessonRepository, "Collocations", "Adjective + Noun Collocations", orderIndex++, getAdjectiveNounCollocationsJson());
        saveOrUpdate(lessonRepository, "Collocations", "Adverb + Adjective Collocations", orderIndex++, getAdverbAdjectiveCollocationsJson());

        // 10. Word of the Day
        saveOrUpdate(lessonRepository, "Word of the Day", "Daily featured word feature", orderIndex++, getWordOfTheDayJson());

        log.info("Vocabulary lesson seeding and updates completed successfully.");
    }

    private static void saveOrUpdate(LessonRepository repo, String section, String title, int orderIndex, String jsonContent) {
        Optional<Lesson> opt = repo.findByModuleAndTitle("VOCABULARY", title);
        if (opt.isPresent()) {
            Lesson l = opt.get();
            l.setSection(section);
            l.setOrderIndex(orderIndex);
            // Overwrite if it is placeholder or content is updated
            if (l.getContent().contains("This lesson content will be updated soon") || !l.getContent().equals(jsonContent)) {
                l.setContent(jsonContent);
                repo.save(l);
            }
        } else {
            repo.save(Lesson.builder()
                    .module("VOCABULARY")
                    .section(section)
                    .title(title)
                    .orderIndex(orderIndex)
                    .content(jsonContent)
                    .build());
        }
    }

    // --- JSON Contents ---

    private static String getWhatIsVocabularyJson() {
        return """
        {
          "title": "What is Vocabulary",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Introduction\\n\\nVocabulary is the collection of words a person knows and uses. While grammar gives you the rules for combining words, vocabulary gives you the actual words to communicate ideas, emotions, and information. A strong vocabulary allows you to express yourself precisely, understand others better, and sound more confident and professional — especially crucial for interviews, group discussions, and workplace communication.\\n\\n### Context and Connection\\n\\nWhen you speak, words are your building blocks. Without vocabulary, grammar is just a structure with nothing to fill it. Learning vocabulary isn't just about reading lists of words; it's about seeing how words behave in different circumstances, what emotional tone they carry, and which words they naturally pair with.",
          "practice": {
            "question": "What is vocabulary?",
            "options": [
              "The rules of combining words in a sentence.",
              "The collection of words a person knows and uses.",
              "A list of grammatical errors to avoid.",
              "The style of writing code in software engineering."
            ],
            "answer": "The collection of words a person knows and uses."
          }
        }
        """;
    }

    private static String getWhyVocabularyMattersJson() {
        return """
        {
          "title": "Why Vocabulary Matters",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Why Vocabulary Matters\\n\\n*   **Precision**: The right word conveys exact meaning. *'Happy'* vs *'ecstatic'* vs *'content'* — each carries a different shade of meaning.\\n*   **Confidence**: A wider vocabulary reduces hesitation while speaking, since you're not stuck searching for words.\\n*   **Comprehension**: Understanding more words means understanding more of what you read and hear, including in interviews, news, and technical documents.\\n*   **Professional Impression**: Using varied, appropriate vocabulary (not repeating *'good'* and *'nice'* constantly) signals education and articulateness.",
          "practice": {
            "question": "Which of the following is a direct benefit of having a rich vocabulary?",
            "options": [
              "It completely eliminates the need to speak.",
              "It reduces hesitation while speaking because you do not get stuck searching for words.",
              "It automatically fixes your pronunciation accents.",
              "It only helps in written exams, not verbal communication."
            ],
            "answer": "It reduces hesitation while speaking because you do not get stuck searching for words."
          }
        }
        """;
    }

    private static String getVocabularyLearningStrategiesJson() {
        return """
        {
          "title": "Vocabulary Learning Strategies",
          "level": "Beginner",
          "duration": "15 min",
          "xp": "+50 XP",
          "markdown": "### Vocabulary Learning Strategies\\n\\n*   **Read widely** — newspapers, articles, books expose you to words in context.\\n*   **Learn word families** — knowing *'decide'* helps you learn *'decision,'* *'decisive,'* *'decisively'* together.\\n*   **Use spaced repetition** — review new words at increasing intervals (1 day, 3 days, 1 week) to move them into long-term memory.\\n*   **Learn in context, not isolation** — memorize words within example sentences, not as standalone definitions.\\n*   **Active usage** — try using 2-3 new words in conversation or writing each day.\\n*   **Group by theme** — learning *'confused'* and *'puzzled'* and *'baffled'* together as synonyms is more efficient than learning them separately.",
          "practice": {
            "question": "What is spaced repetition in vocabulary learning?",
            "options": [
              "Reviewing new words at increasing intervals (e.g., 1 day, 3 days, 1 week) to improve long-term retention.",
              "Writing a word 100 times in a single session.",
              "Searching for synonyms in alphabetical order.",
              "Ignoring a word until you hear it spoken on television."
            ],
            "answer": "Reviewing new words at increasing intervals (e.g., 1 day, 3 days, 1 week) to improve long-term retention."
          }
        }
        """;
    }

    private static String getNounFormsJson() {
        return """
        {
          "title": "Noun Forms",
          "level": "Beginner",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Common Noun-Forming Suffixes\\n\\nMany English words change form depending on which part of speech is needed in a sentence. Knowing common noun suffixes helps you recognize and build nouns easily.\\n\\n*   **-tion / -sion**: *decide* to *decision*, *inform* to *information*\\n*   **-ment**: *develop* to *development*, *argue* to *argument*\\n*   **-ness**: *happy* to *happiness*, *kind* to *kindness*\\n*   **-ity**: *active* to *activity*, *real* to *reality*\\n*   **-ance / -ence**: *perform* to *performance*, *differ* to *difference*\\n*   **-er / -or**: *teach* to *teacher*, *act* to *actor*\\n*   **-ist**: *art* to *artist*, *science* to *scientist*",
          "practice": {
            "question": "What is the noun form of the verb 'differ'?",
            "options": [
              "different",
              "difference",
              "differently",
              "differential"
            ],
            "answer": "difference"
          }
        }
        """;
    }

    private static String getVerbFormsJson() {
        return """
        {
          "title": "Verb Forms",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Common Verb-Forming Suffixes\\n\\nVerbs represent actions or states of being. We can create verbs from nouns or adjectives using specific suffixes:\\n\\n*   **-ify**: *simple* to *simplify*, *class* to *classify*\\n*   **-ize / -ise**: *real* to *realize*, *modern* to *modernize*\\n*   **-ate**: *active* to *activate*, *different* to *differentiate*\\n*   **-en**: *wide* to *widen*, *short* to *shorten*\\n\\nRecognizing these changes helps you adapt a concept to fit the grammatical needs of your sentences.",
          "practice": {
            "question": "What is the verb form of the adjective 'simple'?",
            "options": [
              "simplicity",
              "simplification",
              "simplify",
              "simply"
            ],
            "answer": "simplify"
          }
        }
        """;
    }

    private static String getAdjectiveFormsJson() {
        return """
        {
          "title": "Adjective Forms",
          "level": "Beginner",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Common Adjective-Forming Suffixes\\n\\nAdjectives modify nouns to describe qualities, characteristics, or states. Here are common suffixes used to build adjectives:\\n\\n*   **-ful**: *help* to *helpful*, *care* to *careful*\\n*   **-less**: *hope* to *hopeless*, *care* to *careless*\\n*   **-ous**: *danger* to *dangerous*, *fame* to *famous*\\n*   **-al**: *nation* to *national*, *culture* to *cultural*\\n*   **-ive**: *create* to *creative*, *act* to *active*\\n*   **-able / -ible**: *accept* to *acceptable*, *respond* to *responsible*",
          "practice": {
            "question": "What is the adjective form of the noun 'danger'?",
            "options": [
              "dangerously",
              "endanger",
              "dangerous",
              "dangering"
            ],
            "answer": "dangerous"
          }
        }
        """;
    }

    private static String getAdverbFormsJson() {
        return """
        {
          "title": "Adverb Forms",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Common Adverb Formation\\n\\nAdverbs modify verbs, adjectives, or other adverbs. Most adverbs are formed by adding **-ly** to an adjective:\\n\\n*   *quick* to *quickly*\\n*   *careful* to *carefully*\\n*   *happy* to *happily* (note the 'y' changes to 'i' before adding 'ly')\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'This is very importantly.'\\n*   **Correct**: 'This is very *important*.' (we need an adjective form after the linking verb 'is', not an adverb)",
          "practice": {
            "question": "What is the adverb form of 'happy'?",
            "options": [
              "happiness",
              "happier",
              "happily",
              "happying"
            ],
            "answer": "happily"
          }
        }
        """;
    }

    private static String getWordFamiliesJson() {
        return """
        {
          "title": "Word Families",
          "level": "Beginner",
          "duration": "15 min",
          "xp": "+50 XP",
          "markdown": "### Word Families\\n\\nRecognizing word families (words sharing the same root but differing in form) dramatically speeds up vocabulary building. Learning one root word often gives you several related words for free.\\n\\n### Word Family Example Table\\n\\n| Verb | Noun | Adjective | Adverb |\\n| :--- | :--- | :--- | :--- |\\n| *create* | creation / creativity | creative | creatively |\\n| *decide* | decision | decisive | decisively |\\n| *succeed* | success | successful | successfully |\\n| *please* | pleasure | pleasant | pleasantly |\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'She is a success person.'\\n*   **Correct**: 'She is a *successful* person.' (need an adjective form to modify 'person')",
          "practice": {
            "question": "Fill in the blank: 'She solved the problem ______.'",
            "options": [
              "creative",
              "creation",
              "creatively",
              "creativity"
            ],
            "answer": "creatively"
          }
        }
        """;
    }

    private static String getEverydayObjectsPeopleJson() {
        return """
        {
          "title": "Everyday Objects & People",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Everyday Objects & People\\n\\nMastering these essential daily terms is key for natural, casual communication:\\n\\n*   **wallet** — a pocket-sized flat folding case for holding money and cards.\\n*   **keys** — metal instruments used to lock or unlock doors.\\n*   **charger** — a device for charging a battery.\\n*   **umbrella** — a folding canopy designed to protect against rain or sunlight.\\n*   **spectacles** — eyeglasses worn to correct vision.\\n*   **colleague** — a person with whom one works in a profession or business.\\n*   **neighbor** — a person living near or next door.\\n*   **stranger** — a person whom one does not know or is unfamiliar with.\\n*   **acquaintance** — a person one knows slightly, but who is not a close friend.\\n*   **roommate** — a person with whom one shares a bedroom, apartment, or house.",
          "practice": {
            "question": "What is the word for a person you work with?",
            "options": [
              "neighbor",
              "colleague",
              "acquaintance",
              "stranger"
            ],
            "answer": "colleague"
          }
        }
        """;
    }

    private static String getBodyHealthFoodJson() {
        return """
        {
          "title": "Body, Health & Food",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Body, Health & Food\\n\\nEssential vocabulary for talking about wellness, meals, and eating habits:\\n\\n*   **headache** — a continuous pain in the head.\\n*   **fever** — an abnormally high body temperature.\\n*   **appetite** — a natural desire to satisfy a bodily need, especially for food.\\n*   **nutritious** — efficient as food; nourishing.\\n*   **ingredient** — any of the foods or substances that are combined to make a particular dish.\\n*   **recipe** — a set of instructions for preparing a particular dish.\\n*   **leftover** — something, especially food, remaining after the rest has been used or consumed.\\n*   **snack** — a small amount of food eaten between meals.\\n*   **beverage** — a drink other than water.\\n*   **cuisine** — a style or method of cooking, especially as characteristic of a particular country or region.",
          "practice": {
            "question": "Which word refers to a drink other than water?",
            "options": [
              "cuisine",
              "beverage",
              "ingredient",
              "snack"
            ],
            "answer": "beverage"
          }
        }
        """;
    }

    private static String getHomeWorkTravelJson() {
        return """
        {
          "title": "Home, Work & Travel",
          "level": "Beginner",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Home, Work & Travel\\n\\nEssential vocabulary for describing places, professional settings, and trips:\\n\\n*   **apartment** — a suite of rooms forming one residence.\\n*   **landlord** — a person who rents land, a building, or an apartment to a tenant.\\n*   **tenant** — a person who occupies land or property rented from a landlord.\\n*   **commute** — travel some distance between one's home and place of work on a regular basis.\\n*   **deadline** — the latest time or date by which something should be completed.\\n*   **itinerary** — a planned route or journey.\\n*   **luggage** — suitcases and bags containing personal belongings for travel.\\n*   **boarding pass** — a pass for boarding an aircraft.\\n*   **layover** — a period of rest or waiting before a further stage in a journey.",
          "practice": {
            "question": "What is the term for a temporary stop between flights?",
            "options": [
              "itinerary",
              "commute",
              "layover",
              "boarding pass"
            ],
            "answer": "layover"
          }
        }
        """;
    }

    private static String getNatureTimeFeelingsJson() {
        return """
        {
          "title": "Nature, Time & Feelings",
          "level": "Beginner",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Nature, Time & Feelings\\n\\nEssential words to describe your environment and emotional state:\\n\\n*   **drizzle** — light rain falling in very fine drops.\\n*   **humid** — marked by a relatively high level of water vapor in the atmosphere.\\n*   **breeze** — a gentle wind.\\n*   **thunderstorm** — a storm with thunder and lightning, and typically heavy rain.\\n*   **dawn** — the first appearance of light in the sky before sunrise.\\n*   **dusk** — the darker stage of twilight in the evening.\\n*   **overwhelmed** — having a strong emotional effect; feeling overloaded.\\n*   **relieved** — no longer feeling distressed or anxious.\\n*   **anxious** — experiencing worry, unease, or nervousness.\\n*   **content** — in a state of peaceful happiness and satisfaction.\\n*   **frustrated** — feeling annoyed or disappointed because of inability to achieve something.",
          "practice": {
            "question": "Which word describes feeling both happy and relaxed or satisfied?",
            "options": [
              "anxious",
              "content",
              "frustrated",
              "overwhelmed"
            ],
            "answer": "content"
          }
        }
        """;
    }

    private static String getBeginnerA1Json() {
        return """
        {
          "title": "Beginner (A1) Words",
          "level": "Beginner",
          "duration": "8 min",
          "xp": "+50 XP",
          "markdown": "### Beginner (A1) Words\\n\\nThese are the absolute basic foundational adjectives of the English language. They describe size, speed, emotion, and quality in simple terms:\\n\\n*   **happy** / **sad**\\n*   **big** / **small**\\n*   **good** / **bad**\\n*   **easy** / **difficult**\\n*   **fast** / **slow**\\n\\n### Context and Usage\\n\\nWhile useful, relying solely on these words in professional settings can make your speech sound repetitive. Always look for opportunities to upgrade them as you advance.",
          "practice": {
            "question": "Which word is a basic synonym of 'challenging'?",
            "options": [
              "easy",
              "difficult",
              "fast",
              "happy"
            ],
            "answer": "difficult"
          }
        }
        """;
    }

    private static String getElementaryA2Json() {
        return """
        {
          "title": "Elementary (A2) Words",
          "level": "Beginner",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Elementary (A2) Words\\n\\nThese words add nuance to your daily conversations, allowing you to describe reliability, convenience, and variation:\\n\\n*   **comfortable** — providing physical ease and relaxation.\\n*   **convenient** — fitting in well with a person's needs, activities, and plans.\\n*   **reliable** — consistently good in quality or performance; able to be trusted.\\n*   **obvious** — easily perceived or understood; clear, self-evident.\\n*   **particular** — detailing a specific single item, group, or detail.\\n*   **similar** — having a resemblance in appearance, character, or quantity.\\n*   **various** — of different kinds, as two or more things.\\n*   **available** — able to be used or obtained; at hand.",
          "practice": {
            "question": "Which word describes something that is easily understood or clear?",
            "options": [
              "obvious",
              "particular",
              "similar",
              "various"
            ],
            "answer": "obvious"
          }
        }
        """;
    }

    private static String getIntermediateB1Json() {
        return """
        {
          "title": "Intermediate (B1) Words",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Intermediate (B1) Words\\n\\nThese words are crucial for professional communication, helping you explain reasoning, impact, and conclusions in interviews:\\n\\n*   **significant** — sufficiently great or important to be worthy of attention; noteworthy.\\n*   **essential** — absolutely necessary; extremely important.\\n*   **considerable** — notably large in size, amount, or extent.\\n*   **approach** — a way of dealing with something or someone.\\n*   **aspect** — a particular part or feature of something.\\n*   **factor** — a circumstance, fact, or influence that contributes to a result.\\n*   **evidence** — the available body of facts indicating whether a belief is true.\\n*   **ultimately** — in the end; at the most basic level.",
          "practice": {
            "question": "Which word is a synonym for 'absolutely necessary'?",
            "options": [
              "considerable",
              "essential",
              "aspect",
              "approach"
            ],
            "answer": "essential"
          }
        }
        """;
    }

    private static String getAdvancedB2C1Json() {
        return """
        {
          "title": "Advanced (B2/C1) Words",
          "level": "Advanced",
          "duration": "15 min",
          "xp": "+50 XP",
          "markdown": "### Advanced (B2/C1) Words\\n\\nUsing B2/C1 level vocabulary shows high articulation and sophistication in interviews and public speaking.\\n\\n### Upper-Intermediate (B2)\\n*   **inevitable** — certain to happen; unavoidable.\\n*   **comprehensive** — complete; including all or nearly all elements or aspects.\\n*   **substantial** — of considerable importance, size, or worth.\\n*   **controversial** — giving rise to public disagreement.\\n*   **plausible** — seeming reasonable or probable.\\n*   **ambiguous** — open to more than one interpretation; having a double meaning.\\n*   **coherent** — logical and consistent.\\n\\n### Advanced (C1)\\n*   **ubiquitous** — present, appearing, or found everywhere.\\n*   **meticulous** — showing great attention to detail; very careful and precise.\\n*   **pragmatic** — dealing with things sensibly and realistically based on practical factors.\\n*   **ephemeral** — lasting for a very short time.\\n*   **paradigm** — a typical pattern or model of something.\\n*   **discern** — distinguish or perceive with difficulty.\\n*   **mitigate** — make less severe, serious, or painful.\\n*   **nuanced** — characterized by subtle shades of meaning or expression.",
          "practice": {
            "question": "Which C1 word means 'present, appearing, or found everywhere'?",
            "options": [
              "meticulous",
              "ubiquitous",
              "ephemeral",
              "pragmatic"
            ],
            "answer": "ubiquitous"
          }
        }
        """;
    }

    private static String getCommonSynonymPairsJson() {
        return """
        {
          "title": "Common Synonym Pairs",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Common Synonym Pairs\\n\\nKnowing synonyms allows you to avoid repetition and vary your language in conversations, presentations, and interviews:\\n\\n*   **Happy**: *Joyful, Delighted, Pleased, Cheerful*\\n*   **Sad**: *Unhappy, Sorrowful, Dejected, Gloomy*\\n*   **Beautiful**: *Gorgeous, Stunning, Attractive, Lovely*\\n*   **Difficult**: *Challenging, Tough, Demanding, Arduous*\\n*   **Important**: *Crucial, Vital, Significant, Essential*\\n*   **Fast**: *Quick, Rapid, Swift, Speedy*",
          "practice": {
            "question": "Which of the following is a formal synonym for 'important'?",
            "options": [
              "happy",
              "crucial",
              "gloomy",
              "arduous"
            ],
            "answer": "crucial"
          }
        }
        """;
    }

    private static String getCommonAntonymPairsJson() {
        return """
        {
          "title": "Common Antonym Pairs",
          "level": "Beginner",
          "duration": "8 min",
          "xp": "+50 XP",
          "markdown": "### Common Antonym Pairs\\n\\nAntonyms help you express contrast, comparisons, and opposites clearly. Here are the core basic pairs:\\n\\n*   **Hot** vs **Cold**\\n*   **Big** vs **Small**\\n*   **Fast** vs **Slow**\\n*   **Happy** vs **Sad**\\n*   **Strong** vs **Weak**\\n*   **Rich** vs **Poor**",
          "practice": {
            "question": "What is the antonym of the word 'strong'?",
            "options": [
              "weak",
              "rich",
              "fast",
              "cold"
            ],
            "answer": "weak"
          }
        }
        """;
    }

    private static String getSynonymPrecisionJson() {
        return """
        {
          "title": "Synonym Precision (formal vs informal)",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Synonym Precision\\n\\nNot all synonyms are interchangeable in every context. Selecting the right word based on formality is a key communication skill.\\n\\n### Formal vs Informal Pairs\\n\\n*   **purchase** (Formal) vs **buy** (Informal)\\n*   **commence** (Formal) vs **start** (Informal)\\n*   **assistance** (Formal) vs **help** (Informal)\\n*   **residence** (Formal) vs **house/home** (Informal)\\n\\n### Common Mistakes\\n\\nUsing *'gorgeous'* for a simple object: 'The chair is gorgeous' sounds odd — use *'beautiful/nice'* for simple objects; *'gorgeous'* fits better for people, clothing, or scenery.",
          "practice": {
            "question": "What is the formal synonym for 'start'?",
            "options": [
              "buy",
              "commence",
              "assistance",
              "residence"
            ],
            "answer": "commence"
          }
        }
        """;
    }

    private static String getLookPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with LOOK",
          "level": "Intermediate",
          "duration": "15 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "look after",
              "pos": "verb",
              "ipa": "/lʊk ˈæf.tər/",
              "definition": "To take care of someone or something.",
              "meanings": [
                {"num": 1, "context": "Caregiving", "sentence": "She looks after her younger brother."}
              ],
              "memory_tip": "Think of watching over someone's path behind them to keep them safe.",
              "synonyms": ["take care of", "mind"],
              "antonyms": ["neglect"]
            },
            {
              "word": "look for",
              "pos": "verb",
              "ipa": "/lʊk fɔːr/",
              "definition": "To search for something.",
              "meanings": [
                {"num": 1, "context": "Searching", "sentence": "I'm looking for my keys."}
              ],
              "memory_tip": "You are 'for' finding it, so you search.",
              "synonyms": ["search", "seek"],
              "antonyms": ["find", "hide"]
            },
            {
              "word": "look up",
              "pos": "verb",
              "ipa": "/lʊk ʌp/",
              "definition": "To search for information in a reference book or database; to improve.",
              "meanings": [
                {"num": 1, "context": "Research", "sentence": "Look up the word in a dictionary."},
                {"num": 2, "context": "Improvement", "sentence": "Things are looking up."}
              ],
              "memory_tip": "Lifting your eyes up to scan a bookshelf or sky.",
              "synonyms": ["search", "improve"],
              "antonyms": ["decline"]
            },
            {
              "word": "look into",
              "pos": "verb",
              "ipa": "/lʊk ˈɪn.tuː/",
              "definition": "To investigate or examine.",
              "meanings": [
                {"num": 1, "context": "Investigation", "sentence": "The police are looking into the matter."}
              ],
              "memory_tip": "Peering deeply 'into' a box or case.",
              "synonyms": ["investigate", "examine"],
              "antonyms": ["ignore"]
            },
            {
              "word": "look forward to",
              "pos": "verb",
              "ipa": "/lʊk ˈfɔː.wəd tuː/",
              "definition": "To anticipate something with pleasure.",
              "meanings": [
                {"num": 1, "context": "Anticipation", "sentence": "I look forward to meeting you."}
              ],
              "memory_tip": "Always use an '-ing' verb or noun after look forward to. E.g., 'look forward to meeting' (not 'meet').",
              "synonyms": ["anticipate", "await"],
              "antonyms": ["dread"]
            },
            {
              "word": "look out",
              "pos": "verb",
              "ipa": "/lʊk aʊt/",
              "definition": "To be vigilant or take care.",
              "meanings": [
                {"num": 1, "context": "Caution", "sentence": "Look out! There's a car coming."}
              ],
              "memory_tip": "Sticking your head 'out' of the window to scan for danger.",
              "synonyms": ["watch out", "beware"],
              "antonyms": ["ignore"]
            },
            {
              "word": "look back",
              "pos": "verb",
              "ipa": "/lʊk bæk/",
              "definition": "To think about something that happened in the past.",
              "meanings": [
                {"num": 1, "context": "Reflection", "sentence": "Looking back, I should have studied harder."}
              ],
              "memory_tip": "Physically turning your head 'back'wards to view the road behind.",
              "synonyms": ["reflect", "recollect"],
              "antonyms": ["anticipate"]
            },
            {
              "word": "look down on",
              "pos": "verb",
              "ipa": "/lʊk daʊn ɒn/",
              "definition": "To think that someone or something is inferior.",
              "meanings": [
                {"num": 1, "context": "Superiority", "sentence": "He looks down on people who didn't go to college."}
              ],
              "memory_tip": "Imagining you are standing above others, casting your eyes 'down' on them.",
              "synonyms": ["despise", "disdain"],
              "antonyms": ["respect", "admire"]
            }
          ],
          "practice": {
            "question": "Fill: 'I need to ______ (search info) this word.'",
            "options": [
              "look after",
              "look up",
              "look forward to",
              "look down on"
            ],
            "answer": "look up"
          }
        }
        """;
    }

    private static String getPutPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with PUT",
          "level": "Intermediate",
          "duration": "15 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "put off",
              "pos": "verb",
              "ipa": "/pʊt ɒf/",
              "definition": "To postpone; to delay.",
              "meanings": [
                {"num": 1, "context": "Scheduling", "sentence": "Let's put off the meeting until Monday."}
              ],
              "memory_tip": "Taking something 'off' the current day's plan.",
              "synonyms": ["postpone", "delay"],
              "antonyms": ["advance", "expedite"]
            },
            {
              "word": "put on",
              "pos": "verb",
              "ipa": "/pʊt ɒn/",
              "definition": "To dress oneself in.",
              "meanings": [
                {"num": 1, "context": "Clothing", "sentence": "Put on your jacket."}
              ],
              "memory_tip": "Placing clothes 'on' your skin.",
              "synonyms": ["wear", "don"],
              "antonyms": ["take off"]
            },
            {
              "word": "put up with",
              "pos": "verb",
              "ipa": "/pʊt ʌp wɪð/",
              "definition": "To tolerate or endure without complaining.",
              "meanings": [
                {"num": 1, "context": "Tolerance", "sentence": "I can't put up with this noise anymore."}
              ],
              "memory_tip": "Holding 'up' a heavy shield 'with' someone else.",
              "synonyms": ["tolerate", "endure"],
              "antonyms": ["reject", "oppose"]
            },
            {
              "word": "put down",
              "pos": "verb",
              "ipa": "/pʊt daʊn/",
              "definition": "To place on a surface; to criticize someone.",
              "meanings": [
                {"num": 1, "context": "Placement", "sentence": "Put the bag down."},
                {"num": 2, "context": "Criticism", "sentence": "Stop putting me down."}
              ],
              "memory_tip": "Moving something 'down' to the floor, or demoting someone's feelings.",
              "synonyms": ["place", "criticize"],
              "antonyms": ["lift", "praise"]
            },
            {
              "word": "put away",
              "pos": "verb",
              "ipa": "/pʊt əˈweɪ/",
              "definition": "To store in an appropriate place.",
              "meanings": [
                {"num": 1, "context": "Storage", "sentence": "Put away your toys."}
              ],
              "memory_tip": "Removing items from sight to store them 'away'.",
              "synonyms": ["store", "tidy"],
              "antonyms": ["take out"]
            },
            {
              "word": "put out",
              "pos": "verb",
              "ipa": "/pʊt aʊt/",
              "definition": "To extinguish a fire or light.",
              "meanings": [
                {"num": 1, "context": "Firefighting", "sentence": "Put out the fire."}
              ],
              "memory_tip": "Making the fire go 'out'.",
              "synonyms": ["extinguish", "quench"],
              "antonyms": ["ignite"]
            },
            {
              "word": "put through",
              "pos": "verb",
              "ipa": "/pʊt θruː/",
              "definition": "To connect someone on the phone.",
              "meanings": [
                {"num": 1, "context": "Telephony", "sentence": "Please put me through to the manager."}
              ],
              "memory_tip": "Sending the call signal 'through' the wire.",
              "synonyms": ["connect", "transfer"],
              "antonyms": ["disconnect"]
            }
          ],
          "practice": {
            "question": "Which phrasal verb with PUT means 'to postpone'?",
            "options": [
              "put on",
              "put off",
              "put up with",
              "put through"
            ],
            "answer": "put off"
          }
        }
        """;
    }

    private static String getTurnPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with TURN",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "turn on/off",
              "pos": "verb",
              "ipa": "/tɜːn ɒn / ɒf/",
              "definition": "To activate or deactivate a device or connection.",
              "meanings": [
                {"num": 1, "context": "Electricity", "sentence": "Turn on the lights."}
              ],
              "memory_tip": "Turning a dial to 'on' or 'off'.",
              "synonyms": ["activate", "switch off"],
              "antonyms": ["toggle"]
            },
            {
              "word": "turn up/down",
              "pos": "verb",
              "ipa": "/tɜːn ʌp / daʊn/",
              "definition": "To increase or decrease volume/intensity; to reject an offer (down).",
              "meanings": [
                {"num": 1, "context": "Volume", "sentence": "Turn up the volume."},
                {"num": 2, "context": "Rejection", "sentence": "She turned down the job offer."}
              ],
              "memory_tip": "Moving a lever 'up' or 'down'.",
              "synonyms": ["increase", "reject"],
              "antonyms": ["decrease", "accept"]
            },
            {
              "word": "turn out",
              "pos": "verb",
              "ipa": "/tɜːn aʊt/",
              "definition": "To prove to be; to result in.",
              "meanings": [
                {"num": 1, "context": "Result", "sentence": "The party turned out to be fun."}
              ],
              "memory_tip": "How things 'out'wardly show themselves at the end.",
              "synonyms": ["result", "transpire"],
              "antonyms": []
            },
            {
              "word": "turn in",
              "pos": "verb",
              "ipa": "/tɜːn ɪn/",
              "definition": "To submit something; to go to bed.",
              "meanings": [
                {"num": 1, "context": "Submission", "sentence": "Turn in your homework."},
                {"num": 2, "context": "Rest", "sentence": "I'm turning in early tonight."}
              ],
              "memory_tip": "Handing papers 'in' to a box, or folding yourself 'in' under bed covers.",
              "synonyms": ["submit", "go to sleep"],
              "antonyms": ["take out"]
            },
            {
              "word": "turn around",
              "pos": "verb",
              "ipa": "/tɜːn əˈraʊnd/",
              "definition": "To reverse direction; to improve a failing situation.",
              "meanings": [
                {"num": 1, "context": "Improvement", "sentence": "The company turned around after new leadership."}
              ],
              "memory_tip": "Rotating 180 degrees to go the right way.",
              "synonyms": ["reverse", "improve"],
              "antonyms": ["decline"]
            }
          ],
          "practice": {
            "question": "What does it mean if someone 'turned down' a job proposal?",
            "options": [
              "They accepted it gladly.",
              "They postponed the interview.",
              "They rejected the proposal.",
              "They lost the address."
            ],
            "answer": "They rejected the proposal."
          }
        }
        """;
    }

    private static String getBreakPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with BREAK",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "break down",
              "pos": "verb",
              "ipa": "/breɪk daʊn/",
              "definition": "To stop functioning (machinery); to lose emotional control.",
              "meanings": [
                {"num": 1, "context": "Mechanical", "sentence": "The car broke down."},
                {"num": 2, "context": "Emotional", "sentence": "She broke down in tears."}
              ],
              "memory_tip": "Collapse 'down' to the floor in failure.",
              "synonyms": ["collapse", "fail"],
              "antonyms": ["function", "succeed"]
            },
            {
              "word": "break up",
              "pos": "verb",
              "ipa": "/breɪk ʌp/",
              "definition": "To end a relationship.",
              "meanings": [
                {"num": 1, "context": "Relationships", "sentence": "They broke up last month."}
              ],
              "memory_tip": "Splitting a single piece 'up' into fragmented parts.",
              "synonyms": ["separate", "split"],
              "antonyms": ["marry", "unite"]
            },
            {
              "word": "break in",
              "pos": "verb",
              "ipa": "/breɪk ɪn/",
              "definition": "To enter a building forcibly; to interrupt.",
              "meanings": [
                {"num": 1, "context": "Theft", "sentence": "Thieves broke in last night."}
              ],
              "memory_tip": "Breaking a window or lock to get 'in'.",
              "synonyms": ["intrude", "interrupt"],
              "antonyms": ["exit"]
            },
            {
              "word": "break out",
              "pos": "verb",
              "ipa": "/breɪk aʊt/",
              "definition": "To escape from constraint; to start suddenly (war, fire).",
              "meanings": [
                {"num": 1, "context": "Emergency", "sentence": "A fire broke out in the building."}
              ],
              "memory_tip": "Bursting 'out' of confinement.",
              "synonyms": ["escape", "erupt"],
              "antonyms": ["contain"]
            },
            {
              "word": "break through",
              "pos": "verb",
              "ipa": "/breɪk θruː/",
              "definition": "To overcome an obstacle or barrier.",
              "meanings": [
                {"num": 1, "context": "Achievement", "sentence": "Scientists made a breakthrough in the research."}
              ],
              "memory_tip": "Smashed 'through' a brick wall of difficulty.",
              "synonyms": ["overcome", "penetrate"],
              "antonyms": ["fail"]
            }
          ],
          "practice": {
            "question": "What is the meaning of 'break up'?",
            "options": [
              "To start a new project.",
              "To enter a house forcibly.",
              "To end a relationship.",
              "To fix a broken car."
            ],
            "answer": "To end a relationship."
          }
        }
        """;
    }

    private static String getGivePhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with GIVE",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "give up",
              "pos": "verb",
              "ipa": "/ɡɪv ʌp/",
              "definition": "To quit; to stop making an effort.",
              "meanings": [
                {"num": 1, "context": "Effort", "sentence": "Don't give up on your dreams."}
              ],
              "memory_tip": "Raising your hands 'up' in surrender.",
              "synonyms": ["quit", "surrender"],
              "antonyms": ["persist", "continue"]
            },
            {
              "word": "give in",
              "pos": "verb",
              "ipa": "/ɡɪv ɪn/",
              "definition": "To surrender; to agree reluctantly after opposition.",
              "meanings": [
                {"num": 1, "context": "Submission", "sentence": "She finally gave in to his request."}
              ],
              "memory_tip": "Handing 'in' your flag of defiance.",
              "synonyms": ["yield", "comply"],
              "antonyms": ["resist"]
            },
            {
              "word": "give away",
              "pos": "verb",
              "ipa": "/ɡɪv əˈweɪ/",
              "definition": "To donate or hand over for free; to reveal a secret.",
              "meanings": [
                {"num": 1, "context": "Donation", "sentence": "He gave away all his old books."},
                {"num": 2, "context": "Secrets", "sentence": "Don't give away the secret."}
              ],
              "memory_tip": "Sending things 'away' to others.",
              "synonyms": ["donate", "reveal"],
              "antonyms": ["keep", "hide"]
            },
            {
              "word": "give back",
              "pos": "verb",
              "ipa": "/ɡɪv bæk/",
              "definition": "To return something to its owner.",
              "meanings": [
                {"num": 1, "context": "Returning", "sentence": "Please give back my pen."}
              ],
              "memory_tip": "Returning it 'back' to where it came from.",
              "synonyms": ["return", "restore"],
              "antonyms": ["take", "keep"]
            },
            {
              "word": "give out",
              "pos": "verb",
              "ipa": "/ɡɪv aʊt/",
              "definition": "To distribute; to stop functioning due to exhaustion.",
              "meanings": [
                {"num": 1, "context": "Distribution", "sentence": "They gave out free samples."},
                {"num": 2, "context": "Failure", "sentence": "My phone battery gave out."}
              ],
              "memory_tip": "Sending items 'out' to a crowd.",
              "synonyms": ["distribute", "fail"],
              "antonyms": ["collect"]
            }
          ],
          "practice": {
            "question": "Which phrasal verb means 'to return something to its owner'?",
            "options": [
              "give up",
              "give away",
              "give back",
              "give in"
            ],
            "answer": "give back"
          }
        }
        """;
    }

    private static String getSetPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with SET",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "set up",
              "pos": "verb",
              "ipa": "/set ʌp/",
              "definition": "To establish or arrange something (business, system, equipment).",
              "meanings": [
                {"num": 1, "context": "Business", "sentence": "They set up a new business."}
              ],
              "memory_tip": "Laying pieces 'up'ward to build a structure.",
              "synonyms": ["establish", "arrange"],
              "antonyms": ["dismantle"]
            },
            {
              "word": "set off",
              "pos": "verb",
              "ipa": "/set ɒf/",
              "definition": "To start a journey; to trigger an alarm or reaction.",
              "meanings": [
                {"num": 1, "context": "Travel", "sentence": "We set off early in the morning."}
              ],
              "memory_tip": "Stepping 'off' your front porch to start walking.",
              "synonyms": ["depart", "trigger"],
              "antonyms": ["arrive"]
            },
            {
              "word": "set out",
              "pos": "verb",
              "ipa": "/set aʊt/",
              "definition": "To begin a task or journey with a specific goal in mind.",
              "meanings": [
                {"num": 1, "context": "Ambition", "sentence": "She set out to become a doctor."}
              ],
              "memory_tip": "Stepping 'out' into the world with a targeted plan.",
              "synonyms": ["aim", "undertake"],
              "antonyms": []
            },
            {
              "word": "set back",
              "pos": "verb",
              "ipa": "/set bæk/",
              "definition": "To delay progress; to cost a certain amount.",
              "meanings": [
                {"num": 1, "context": "Delay", "sentence": "The delay set us back two weeks."}
              ],
              "memory_tip": "Moving a chess piece 'back'wards.",
              "synonyms": ["delay", "hinder"],
              "antonyms": ["advance", "accelerate"]
            },
            {
              "word": "set aside",
              "pos": "verb",
              "ipa": "/set əˈsaɪd/",
              "definition": "To save or reserve for later use.",
              "meanings": [
                {"num": 1, "context": "Saving", "sentence": "Set aside some money for emergencies."}
              ],
              "memory_tip": "Placing objects 'aside' on a different shelf for future safety.",
              "synonyms": ["save", "reserve"],
              "antonyms": ["spend", "waste"]
            }
          ],
          "practice": {
            "question": "What is the meaning of 'set up'?",
            "options": [
              "To delay progress.",
              "To establish or arrange.",
              "To start a journey.",
              "To save money."
            ],
            "answer": "To establish or arrange."
          }
        }
        """;
    }

    private static String getRunPhrasalJson() {
        return """
        {
          "title": "Phrasal Verbs with RUN",
          "level": "Intermediate",
          "duration": "15 min",
          "xp": "+50 XP",
          "words": [
            {
              "word": "run out (of)",
              "pos": "verb",
              "ipa": "/rʌn aʊt əv/",
              "definition": "To use up the supply of something; to have no more left.",
              "meanings": [
                {"num": 1, "context": "Supply", "sentence": "We ran out of milk."}
              ],
              "memory_tip": "Liquid 'running out' of a leaking bottle.",
              "synonyms": ["deplete", "exhaust"],
              "antonyms": ["accumulate"]
            },
            {
              "word": "run into",
              "pos": "verb",
              "ipa": "/rʌn ˈɪn.tuː/",
              "definition": "To meet someone unexpectedly; to encounter problems.",
              "meanings": [
                {"num": 1, "context": "Meeting", "sentence": "I ran into my old friend."},
                {"num": 2, "context": "Problems", "sentence": "We ran into trouble."}
              ],
              "memory_tip": "Bumping directly 'into' a wall or person.",
              "synonyms": ["encounter", "bump into"],
              "antonyms": ["avoid"]
            },
            {
              "word": "run away",
              "pos": "verb",
              "ipa": "/rʌn əˈweɪ/",
              "definition": "To escape or flee.",
              "meanings": [
                {"num": 1, "context": "Escape", "sentence": "The dog ran away from home."}
              ],
              "memory_tip": "Running 'away' to hide.",
              "synonyms": ["flee", "escape"],
              "antonyms": ["confront", "stay"]
            },
            {
              "word": "run over",
              "pos": "verb",
              "ipa": "/rʌn ˈəʊ.vər/",
              "definition": "To hit or drive over with a vehicle; to review or run through quickly.",
              "meanings": [
                {"num": 1, "context": "Accident", "sentence": "The car ran over a pothole."},
                {"num": 2, "context": "Review", "sentence": "Let's run over the plan again."}
              ],
              "memory_tip": "Scanning your eyes 'over' a page of text.",
              "synonyms": ["review", "hit"],
              "antonyms": []
            },
            {
              "word": "run through",
              "pos": "verb",
              "ipa": "/rʌn θruː/",
              "definition": "To practice or review quickly.",
              "meanings": [
                {"num": 1, "context": "Practice", "sentence": "Let's run through the presentation once more."}
              ],
              "memory_tip": "Running 'through' a script before a play.",
              "synonyms": ["review", "rehearse"],
              "antonyms": []
            }
          ],
          "practice": {
            "question": "Which phrasal verb means 'to meet unexpectedly'?",
            "options": [
              "run away",
              "run over",
              "run into",
              "run out"
            ],
            "answer": "run into"
          }
        }
        """;
    }

    private static String getBodyPartIdiomsJson() {
        return """
        {
          "title": "Body Part Idioms",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Body Part Idioms\\n\\nIdioms are phrases whose meaning cannot be understood from the literal definition of individual words. They add color and natural flow to English speech:\\n\\n*   **Break a leg** — Good luck (commonly said to performers or interview candidates). *'Break a leg on your interview tomorrow!'*\\n*   **Cost an arm and a leg** — Extremely expensive. *'That phone costs an arm and a leg.'*\\n*   **Keep an eye on** — Watch carefully. *'Keep an eye on the kids.'*\\n*   **Get cold feet** — Become nervous or hesitant before doing something big. *'He got cold feet before the wedding.'*\\n*   **Head over heels** — Deeply in love. *'She's head over heels for him.'*\\n*   **Lend a hand** — Help. *'Can you lend a hand with this?'*",
          "practice": {
            "question": "What is the meaning of 'cost an arm and a leg'?",
            "options": [
              "Extremely expensive.",
              "Physically damaging.",
              "Very cheap.",
              "Free of charge."
            ],
            "answer": "Extremely expensive."
          }
        }
        """;
    }

    private static String getAnimalIdiomsJson() {
        return """
        {
          "title": "Animal Idioms",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Animal Idioms\\n\\nIdioms featuring animals are common in everyday conversation and professional analogies:\\n\\n*   **Let the cat out of the bag** — Reveal a secret, often accidentally. *'She let the cat out of the bag about the surprise party.'*\\n*   **Kill two birds with one stone** — Accomplish two goals at once. *'By walking to work, I kill two birds with one stone (getting exercise and saving commute fare).'*\\n*   **The elephant in the room** — An obvious problem that everyone knows about but no one wants to discuss. *'We need to talk about the elephant in the room.'*\\n*   **A piece of cake** — Very easy. *'The exam was a piece of cake.'*",
          "practice": {
            "question": "What does 'a piece of cake' mean?",
            "options": [
              "Something very delicious.",
              "Something very easy.",
              "A portion of dessert.",
              "A hard problem to solve."
            ],
            "answer": "Something very easy."
          }
        }
        """;
    }

    private static String getColorIdiomsJson() {
        return """
        {
          "title": "Color Idioms",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Color Idioms\\n\\nIdioms based on colors represent emotional states, rarity, and catches:\\n\\n*   **Once in a blue moon** — Very rarely. *'I eat fast food once in a blue moon.'*\\n*   **Green with envy** — Extremely jealous. *'She was green with envy over his new car.'*\\n*   **Caught red-handed** — Caught in the act of doing something wrong or illegal. *'He was caught red-handed stealing.'*\\n*   **Feeling blue** — Feeling sad or depressed. *'I've been feeling blue lately.'*",
          "practice": {
            "question": "What does 'once in a blue moon' mean?",
            "options": [
              "Every night.",
              "Very rarely.",
              "During a solar eclipse.",
              "Frequently."
            ],
            "answer": "Very rarely."
          }
        }
        """;
    }

    private static String getTimeIdiomsJson() {
        return """
        {
          "title": "Time Idioms",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Time Idioms\\n\\nIdioms concerning urgency, frequency, and operations:\\n\\n*   **In the nick of time** — Just in time; at the last possible moment. *'We arrived in the nick of time.'*\\n*   **Once in a while** — Occasionally; not regularly. *'We go out to dinner once in a while.'*\\n*   **Around the clock** — All day and all night without stopping. *'The hospital operates around the clock.'*",
          "practice": {
            "question": "What does it mean to do something 'around the clock'?",
            "options": [
              "Only at 12 o'clock.",
              "All day and night without stopping.",
              "Rarely.",
              "Whenever you have free time."
            ],
            "answer": "All day and night without stopping."
          }
        }
        """;
    }

    private static String getWorkSuccessIdiomsJson() {
        return """
        {
          "title": "Work & Success Idioms",
          "level": "Intermediate",
          "duration": "12 min",
          "xp": "+50 XP",
          "markdown": "### Work & Success Idioms\\n\\nThese professional idioms are highly popular in corporate environments and job interviews:\\n\\n*   **Hit the ground running** — Start a new activity quickly and with great energy and success. *'The new employee hit the ground running.'*\\n*   **Go the extra mile** — Do more than what is required or expected. *'She always goes the extra mile for clients.'*\\n*   **Touch base** — Make brief contact with someone to update them. *'Let's touch base next week.'*",
          "practice": {
            "question": "What does it mean to 'go the extra mile'?",
            "options": [
              "To run a long race.",
              "To travel a long distance.",
              "To do more than what is required or expected.",
              "To waste time on unnecessary tasks."
            ],
            "answer": "To do more than what is required or expected."
          }
        }
        """;
    }

    private static String getConfusingWordsJson() {
        return """
        {
          "title": "Since vs For, Much vs Many, Make vs Do, Say vs Tell, Affect vs Effect, and other commonly confused pairs",
          "level": "Intermediate",
          "duration": "15 min",
          "xp": "+50 XP",
          "markdown": "### Commonly Confused Word Pairs\\n\\n| Pair | Difference | Example |\\n| :--- | :--- | :--- |\\n| **Since** vs **For** | *Since* = specific starting point; *For* = duration | 'Since 2020' / 'For 5 years' |\\n| **Much** vs **Many** | *Much* = uncountable; *Many* = countable | 'Much water' / 'Many apples' |\\n| **Make** vs **Do** | *Make* = create/produce; *Do* = perform an activity | 'Make a cake' / 'Do homework' |\\n| **Say** vs **Tell** | *Say* = no personal object needed; *Tell* = needs person | 'She said hello.' / 'She told me a story.' |\\n| **Affect** vs **Effect** | *Affect* = verb (to influence); *Effect* = noun (result) | 'The rain affected the game.' / 'The effect was clear.' |\\n| **Its** vs **It's** | *Its* = possessive; *It's* = it is / it has | 'The dog wagged its tail.' / 'It's raining.' |\\n| **Borrow** vs **Lend** | *Borrow* = to take; *Lend* = to give | 'Can I borrow your pen?' / 'I'll lend you my pen.' |\\n| **Rise** vs **Raise** | *Rise* = intransitive; *Raise* = transitive | 'Prices rise.' / 'Raise your hand.' |\\n| **Fewer** vs **Less** | *Fewer* = countable; *Less* = uncountable | 'Fewer people' / 'Less time' |\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'I have less friends than her.'\\n*   **Correct**: 'I have *fewer* friends than her.' (friends are countable)\\n*   **Wrong**: 'He said me the answer.'\\n*   **Correct**: 'He *told* me the answer.' (tell requires a personal object like 'me')",
          "practice": {
            "question": "Fill: 'Could you ______ me your calculator?'",
            "options": [
              "borrow",
              "lend",
              "borrowed",
              "lending"
            ],
            "answer": "lend"
          }
        }
        """;
    }

    private static String getVerbNounCollocationsJson() {
        return """
        {
          "title": "Verb + Noun Collocations",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Verb + Noun Collocations\\n\\nCollocations are words that naturally go together. Using correct combinations makes your English sound natural and fluent rather than translated:\\n\\n*   **Make**: *make a decision, make a mistake, make progress, make an effort, make money*\\n*   **Do**: *do homework, do business, do damage, do research, do a favor*\\n*   **Have**: *have a meeting, have an idea, have a conversation, have a problem*\\n*   **Take**: *take a break, take a chance, take notes, take action, take time*\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'Do a mistake' → **Correct**: '*Make* a mistake'\\n*   **Wrong**: 'Make homework' → **Correct**: '*Do* homework'",
          "practice": {
            "question": "Which combination is correct?",
            "options": [
              "do a mistake",
              "make a mistake",
              "make homework",
              "do a decision"
            ],
            "answer": "make a mistake"
          }
        }
        """;
    }

    private static String getAdjectiveNounCollocationsJson() {
        return """
        {
          "title": "Adjective + Noun Collocations",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Adjective + Noun Collocations\\n\\nUsing standard adjectives with specific nouns is critical for sounding natural:\\n\\n*   **Strong**: *strong coffee, strong tea, strong wind, strong accent*\\n*   **Heavy**: *heavy rain, heavy traffic, heavy smoker*\\n*   **High**: *high risk, high demand, high quality*\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'Strong rain' → **Correct**: '*Heavy* rain'\\n*   **Wrong**: 'Strong traffic' → **Correct**: '*Heavy* traffic'",
          "practice": {
            "question": "Fill: 'We got late because of ______ traffic.'",
            "options": [
              "strong",
              "heavy",
              "high",
              "big"
            ],
            "answer": "heavy"
          }
        }
        """;
    }

    private static String getAdverbAdjectiveCollocationsJson() {
        return """
        {
          "title": "Adverb + Adjective Collocations",
          "level": "Intermediate",
          "duration": "10 min",
          "xp": "+50 XP",
          "markdown": "### Adverb + Adjective Collocations\\n\\nThese combinations intensify or describe qualities with precision:\\n\\n*   **Deeply**: *deeply sorry, deeply moved, deeply concerned*\\n*   **Highly**: *highly likely, highly recommended, highly effective*\\n*   **Fully**: *fully aware, fully booked, fully functional*\\n*   **Completely**: *completely wrong, completely different, completely ruined*\\n*   **Perfectly**: *perfectly clear, perfectly normal, perfectly fine*\\n*   **Badly**: *badly injured, badly needed*\\n\\n### Common Mistakes\\n\\n*   **Wrong**: 'Very likely' (while common, *'highly likely'* is the stronger collocation for academic/formal use)\\n*   **Wrong**: 'Perfect sorry' → **Correct**: '*Deeply* sorry'",
          "practice": {
            "question": "Fill: 'I am ______ sorry for the misunderstanding.'",
            "options": [
              "deeply",
              "badly",
              "highly",
              "perfectly"
            ],
            "answer": "deeply"
          }
        }
        """;
    }

    private static String getWordOfTheDayJson() {
        return """
        {
          "title": "Daily featured word feature",
          "level": "Advanced",
          "duration": "8 min",
          "xp": "+50 XP",
          "markdown": "### Word of the Day: Meticulous\\n\\n*   **Pronunciation**: /muh-TIK-yuh-luhs/\\n*   **Part of speech**: adjective\\n*   **Definition**: Showing great attention to detail; very careful and precise.\\n*   **Example**: 'She is meticulous about checking her work for errors before submission.'\\n*   **Synonyms**: *careful, thorough, precise, painstaking*\\n*   **Antonyms**: *careless, sloppy, negligent*\\n\\n### Memory Tip\\n\\nThink of 'meticulous' as being so careful you'd notice even a 'tick' (small mark) out of place.",
          "practice": {
            "question": "What part of speech is 'meticulous'?",
            "options": [
              "Noun",
              "Verb",
              "Adjective",
              "Adverb"
            ],
            "answer": "Adjective"
          }
        }
        """;
    }
}
