package com.speakmate.backend.config;

import com.speakmate.backend.model.entity.Lesson;
import com.speakmate.backend.model.entity.UserLessonProgress;
import com.speakmate.backend.repository.LessonRepository;
import com.speakmate.backend.repository.UserLessonProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LessonDataSeeder implements CommandLineRunner {

    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;

    @Override
    public void run(String... args) {
        log.info("Checking database for legacy lessons to clean up...");
        List<Lesson> existing = lessonRepository.findAll();
        List<Lesson> legacyToDelete = new ArrayList<>();
        List<String> legacyVocabTitles = List.of(
            "Word Types", "Daily English", "Level-wise Vocab", "Synonyms/Antonyms",
            "Idioms", "Confusing Words", "Collocations", "Word of the Day"
        );
        List<String> legacyPronTitles = List.of(
            "Vowels", "Diphthongs", "Consonants", "Minimal Pairs", "Word Stress", "Intonation", "Connected Speech", "Indian English Fixes"
        );
        for (Lesson l : existing) {
            if ("VOCABULARY".equalsIgnoreCase(l.getModule()) && legacyVocabTitles.contains(l.getTitle())) {
                legacyToDelete.add(l);
            }
            if ("PRONUNCIATION".equalsIgnoreCase(l.getModule()) && legacyPronTitles.contains(l.getTitle())) {
                legacyToDelete.add(l);
            }
        }
        if (!legacyToDelete.isEmpty()) {
            log.info("Deleting {} legacy Vocabulary lessons...", legacyToDelete.size());
            List<UserLessonProgress> allProgress = userLessonProgressRepository.findAll();
            List<UserLessonProgress> progressToDelete = new ArrayList<>();
            for (UserLessonProgress p : allProgress) {
                if (legacyToDelete.stream().anyMatch(l -> l.getId().equals(p.getLesson().getId()))) {
                    progressToDelete.add(p);
                }
            }
            if (!progressToDelete.isEmpty()) {
                userLessonProgressRepository.deleteAll(progressToDelete);
                log.info("Deleted {} progress records associated with legacy lessons.", progressToDelete.size());
            }
            lessonRepository.deleteAll(legacyToDelete);
            // Refresh existing list
            existing = lessonRepository.findAll();
        }

        java.util.Set<String> existingKeys = new java.util.HashSet<>();
        for (Lesson l : existing) {
            existingKeys.add(l.getModule().toUpperCase() + "::" + l.getTitle().toUpperCase());
        }

        seedGrammarLessons(existingKeys);
        seedVocabularyLessons(existingKeys);
        seedPronunciationLessons(existingKeys);
        log.info("Lesson seeding check complete! Total lessons in DB: {}", lessonRepository.count());
    }

    private void seedGrammarLessons(java.util.Set<String> existingKeys) {
        List<Lesson> lessons = new ArrayList<>();
        seedFoundationAndNouns(lessons);
        seedPronounsAndVerbs(lessons);
        seedModifiersAndConnectors(lessons);
        seedArticlesAndPresentTenses(lessons);
        seedVoiceSpeechConditionalsModals(lessons);

        List<Lesson> toSave = new ArrayList<>();
        for (Lesson l : lessons) {
            String key = l.getModule().toUpperCase() + "::" + l.getTitle().toUpperCase();
            if (!existingKeys.contains(key)) {
                toSave.add(l);
            }
        }
        if (!toSave.isEmpty()) {
            lessonRepository.saveAll(toSave);
            log.info("Seeded {} new Grammar lessons.", toSave.size());
        }
    }

    private void seedFoundationAndNouns(List<Lesson> lessons) {
        // 1. Foundation - Introduction
        lessons.add(Lesson.builder().module("GRAMMAR").section("Foundation").title("Introduction to Grammar").orderIndex(1)
                .content("""
                {
                  "difficulty": "Beginner",
                  "read_time": "15 min read",
                  "introduction": "### What is Grammar?\\n\\nGrammar is the system of rules that governs how words are combined, arranged, and modified to create meaningful sentences in a language. Think of grammar as the skeleton of a language — without it, words would just be a random pile of sounds with no structure, no clarity, and no way to reliably communicate meaning.\\n\\nEvery language, including English, has its own grammar system — a set of patterns that native speakers often use instinctively, but which learners need to study explicitly. Grammar tells us how to order words in a sentence (word order), how to change words based on time (tense), how to show relationships between things (prepositions), and how to connect ideas (conjunctions).\\n\\nFor students preparing for campus placements and technical interviews, strong grammar isn't just about passing an English test — it directly affects how confident and professional you sound when speaking to interviewers, writing emails, or explaining your projects. A small grammar mistake ('He don't know' instead of 'He doesn't know') can distract a listener from your actual message and create an unprofessional impression, even if your technical knowledge is excellent.\\n\\nThis course starts from the absolute basics and builds up systematically — from understanding what grammar is, to mastering parts of speech, tenses, sentence structure, and advanced grammar patterns like conditionals and passive voice.\\n\\n---\\n\\n### How a Sentence is Built (The Basics)\\n\\nEvery complete English sentence needs, at minimum, a **Subject** (who or what the sentence is about) and a **Verb** (the action or state).\\n\\n*   **Example:** *'Birds fly.'* → Subject: *Birds* | Verb: *fly*\\n\\nMost sentences also include an **Object** (who or what receives the action):\\n\\n*   **Example:** *'She reads books.'* → Subject: *She* | Verb: *reads* | Object: *books*\\n\\nThis basic pattern — **Subject + Verb + Object (SVO)** — is the backbone of English sentence structure and will come up repeatedly throughout this course.\\n\\n---\\n\\n### Quick Overview of the 8 Parts of Speech\\n\\n*   **Noun** — names a person, place, thing, or idea (*Chennai, teacher, happiness*)\\n*   **Pronoun** — replaces a noun (*he, she, it, they*)\\n*   **Verb** — shows action or state (*run, is, become*)\\n*   **Adjective** — describes a noun (*beautiful, tall, quick*)\\n*   **Adverb** — describes a verb, adjective, or another adverb (*quickly, very, well*)\\n*   **Preposition** — shows relationship/position (*in, on, under, between*)\\n*   **Conjunction** — connects words or clauses (*and, but, because*)\\n*   **Interjection** — expresses sudden emotion (*Wow! Oh! Ouch!*)\\n\\n*(Each of these will be covered in full depth in the next lessons of this course.)*\\n\\n---\\n\\n### Grammar in Spoken vs Written English\\n\\nSpoken English is often more relaxed with grammar — native speakers commonly use contractions (*'I'm', 'don't', 'gonna'*) and sometimes drop words in casual conversation. Written English, especially in professional or academic contexts, tends to follow grammar rules more strictly.\\n\\nFor interviews and group discussions, aim for a middle ground: correct grammar, but natural and not overly formal or robotic-sounding.\\n\\n---\\n\\n### Common Myths About Grammar\\n\\n*   **Myth:** *'Grammar rules are the same in every situation.'*\\n    *   **Reality:** Formal writing (reports, emails) follows stricter rules than casual speech.\\n*   **Myth:** *'If I know vocabulary, grammar doesn't matter.'*\\n    *   **Reality:** Even with a huge vocabulary, wrong grammar can make sentences confusing or incorrect (e.g., *'Yesterday I go store'* — vocabulary is fine, but tense is wrong).\\n*   **Myth:** *'Grammar is only about correcting mistakes.'*\\n    *   **Reality:** Grammar is also about building fluency — knowing the rules well enough that correct sentences come naturally without conscious effort.\\n\\n---\\n\\n### How to Use This Course\\n\\n1.  **Lessons build progressively** — start with Parts of Speech before moving to Tenses, since tenses depend on understanding verbs.\\n2.  **Each lesson includes practice exercises** — don't skip these, as active practice is what converts knowledge into fluent usage.\\n3.  **Use the 'Ask AI Teacher' feature** whenever a rule feels confusing — ask specific questions like *'Why is it \\\"doesn't\\\" and not \\\"don't\\\" with he/she?'*\\n4.  **Revisit lessons periodically** — grammar mastery comes from repetition and applying rules in real sentences, not just reading once.",
                  "when_to_use": [
                    "For Communication: Grammar ensures your listener understands exactly what you mean. Compare: 'I eating rice' vs 'I am eating rice' — the second is clear and correct; the first creates confusion about whether you're describing a habit, a current action, or something else.",
                    "For Professional Credibility: In interviews, group discussions, and workplace emails, grammatical accuracy signals attention to detail and education level — even though it has nothing to do with your actual skills, people unconsciously judge competence based on language accuracy.",
                    "For Avoiding Ambiguity: Grammar rules like correct punctuation and word order prevent sentences from having multiple confusing meanings. Example: 'Let's eat, grandma' vs 'Let's eat grandma' — a single comma changes the entire meaning.",
                    "For Confidence: When you know grammar rules well, you speak more fluently and with less hesitation, because you're not constantly second-guessing your sentence structure."
                  ],
                  "tables": [
                    {
                      "title": "The Branches of English Grammar",
                      "headers": ["Branch", "What It Covers"],
                      "rows": [
                        ["Parts of Speech", "The 8 basic word categories (noun, pronoun, verb, adjective, adverb, preposition, conjunction, interjection)"],
                        ["Tenses", "How verbs change to show time (present, past, future — and their continuous/perfect forms)"],
                        ["Sentence Structure", "How words combine into simple, compound, and complex sentences"],
                        ["Voice", "Active voice (subject does the action) vs Passive voice (subject receives the action)"],
                        ["Speech", "Direct speech (exact quoted words) vs Indirect/Reported speech"],
                        ["Articles & Determiners", "Words like 'a,' 'an,' 'the,' 'this,' 'some' that specify nouns"],
                        ["Conditionals", "'If' sentences that describe cause-effect relationships"],
                        ["Modals", "Helping verbs like can, could, may, might, must, should that express ability, possibility, permission, or obligation"]
                      ]
                    }
                  ],
                  "faqs": [
                    {
                      "question": "Do I need to memorize grammar rules, or will I learn them naturally through practice?",
                      "answer": "Both help — understanding the rule speeds up learning, but real fluency comes from repeated practice until correct usage becomes automatic."
                    },
                    {
                      "question": "Is it okay to make grammar mistakes while speaking?",
                      "answer": "Yes, especially while learning — native speakers make mistakes too. The goal is steady improvement, not instant perfection."
                    },
                    {
                      "question": "Which topic should I focus on most for interview preparation?",
                      "answer": "Tenses (especially Present Perfect vs Simple Past) and Subject-Verb Agreement are the most commonly tested and noticed in spoken interviews."
                    },
                    {
                      "question": "How is this different from what I learned in school?",
                      "answer": "This course focuses on practical, spoken-English application for interviews and professional communication, not just exam-based grammar rules."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Identify the verb in the sentence: 'The train arrives at 6 PM.'",
                      "type": "mcq",
                      "options": ["train", "arrives", "at", "6 PM"],
                      "answer": "arrives"
                    },
                    {
                      "question": "Identify the subject in the sentence: 'My sister loves painting.'",
                      "type": "mcq",
                      "options": ["My sister", "loves", "painting", "sister"],
                      "answer": "My sister"
                    },
                    {
                      "question": "True or False: Every complete sentence must have an object.",
                      "type": "mcq",
                      "options": ["True", "False"],
                      "answer": "False"
                    },
                    {
                      "question": "Which sentence is grammatically correct?",
                      "type": "mcq",
                      "options": ["She don't like tea.", "She doesn't like tea."],
                      "answer": "She doesn't like tea."
                    }
                  ],
                  "related_topics": [
                    {"title": "Nouns"}
                  ]
                }
                """).build());

        // 2. Parts of Speech - Nouns
        lessons.add(Lesson.builder().module("GRAMMAR").section("Parts of Speech").title("Nouns").orderIndex(2)
                .content("""
                {
                  "difficulty": "Beginner",
                  "read_time": "12 min read",
                  "introduction": "### What is a Noun?\\n\\nA noun is a naming word. It designates a person, place, thing, or abstract idea. In sentences, nouns act as the subject (the one performing the action) or the object (the receiver of the action).\\n\\nMastering nouns is essential for clear, concise communication. Choosing precise nouns makes your writing punchy. For example, instead of writing 'the person who manages the department,' you can write 'the director.'",
                  "when_to_use": [
                    "To identify people by title or name ('developer', 'Amit').",
                    "To reference specific locations ('Chennai', 'office').",
                    "To name physical items ('server', 'laptop').",
                    "To express abstract concepts ('integrity', 'success')."
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject Noun", "type": "primary"},
                      {"label": "Verb", "type": "secondary"},
                      {"label": "Object Noun", "type": "tertiary"}
                    ],
                    "note": "Nouns can be countable (car, task) or uncountable (information, advice)."
                  },
                  "exceptions": [
                    {
                      "rule": "Nouns ending in 's' are plural.",
                      "exception": "Some singular nouns end in 's' natively.",
                      "example": "'Physics is my favorite subject.' or 'The news was shocking.'"
                    },
                    {
                      "rule": "Uncountable nouns are always singular.",
                      "exception": "Plural forms of uncountable nouns exist to refer to different types or varieties.",
                      "example": "'The waters of the Atlantic' or 'Various foods from Asia.'"
                    }
                  ],
                  "tables": [
                    {
                      "title": "Comprehensive Noun Classifications (15 Examples)",
                      "headers": ["Noun Type", "Word", "Context", "Real-World Sentence Example"],
                      "rows": [
                        ["Proper Noun", "Chennai", "Workplace", "Our developer team is based in Chennai."],
                        ["Proper Noun", "Oracle", "Company", "We migrated our database to Oracle."],
                        ["Proper Noun", "Rajesh", "Interview", "My name is Rajesh and I am a designer."],
                        ["Common Noun", "manager", "Workplace", "The manager approved my leaves."],
                        ["Common Noun", "project", "Formal", "This project is highly critical for us."],
                        ["Common Noun", "strategy", "Meeting", "Our strategy needs alignment."],
                        ["Abstract Noun", "success", "Interview", "We worked hard to guarantee the success of the release."],
                        ["Abstract Noun", "knowledge", "Casual", "He has deep knowledge of AI technologies."],
                        ["Abstract Noun", "patience", "Workplace", "Dealing with clients requires immense patience."],
                        ["Collective Noun", "team", "Meeting", "Our team is collaborating with designers."],
                        ["Collective Noun", "board", "Formal", "The board of directors is meeting today."],
                        ["Collective Noun", "staff", "Workplace", "The entire technical staff is on call."],
                        ["Uncountable", "information", "Workplace", "Please share the credential information securely."],
                        ["Uncountable", "feedback", "Interview", "Constructive feedback helps developers grow."],
                        ["Uncountable", "equipment", "Facility", "All the test equipment is ready in the lab."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Professional Email to Client",
                      "content": "Dear Team, we have gathered all the necessary equipment and hardware. The information you provided helped us complete the task successfully. We appreciate your constructive feedback."
                    },
                    {
                      "context": "Job Interview Answer",
                      "content": "In my previous job, I managed a team of six developers. I was responsible for coordinating project timelines and resolving technical issues. Our team achieved great success."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "I need some advices.",
                      "correct": "I need some advice. (or) I need a piece of advice.",
                      "why": "'Advice' is uncountable; it cannot be pluralized with an 's'."
                    },
                    {
                      "incorrect": "All the hardwares are ready.",
                      "correct": "All the hardware is ready.",
                      "why": "'Hardware' is uncountable. Use 'is' and do not add 's'."
                    },
                    {
                      "incorrect": "I have many works to finish.",
                      "correct": "I have a lot of work to finish. (or) I have many tasks.",
                      "why": "'Work' is uncountable when referring to tasks or jobs. Use 'tasks' or 'a lot of work'."
                    },
                    {
                      "incorrect": "The board of directors are meeting.",
                      "correct": "The board of directors is meeting.",
                      "why": "Collective nouns like 'board' or 'committee' take singular verbs in standard professional writing."
                    },
                    {
                      "incorrect": "The luggage are in the cab.",
                      "correct": "The luggage is in the cab.",
                      "why": "'Luggage' is uncountable and takes a singular verb."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "Is 'furniture' countable or uncountable?",
                      "answer": "It is uncountable. You should say 'some furniture' or 'pieces of furniture', never 'furnitures'."
                    },
                    {
                      "question": "Can proper nouns be pluralized?",
                      "answer": "Generally no, unless referring to a group of people with that name (e.g., 'The Smiths')."
                    },
                    {
                      "question": "How do I make 'software' plural?",
                      "answer": "Use 'software programs' or 'applications'. Never say 'softwares'."
                    },
                    {
                      "question": "Is 'data' singular or plural?",
                      "answer": "In scientific settings, it is plural (singular 'datum'). In standard corporate English, it is used as uncountable with singular verbs ('the data is correct')."
                    },
                    {
                      "question": "What is a compound noun?",
                      "answer": "A noun made of two or more words, like 'software engineer' or 'laptop charger'."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Identify the uncountable noun in the sentence: 'The team needed more information to fix the printer.'",
                      "type": "mcq",
                      "options": ["team", "information", "printer", "none"],
                      "answer": "information"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Which of the following is an uncountable noun?",
                      "options": ["Report", "Feedback", "Suggestion", "Task"],
                      "answer": "Feedback"
                    },
                    {
                      "question": "Which sentence is grammatically correct?",
                      "options": ["The project team is working hard.", "The project team are working hard.", "The project team were working hard."],
                      "answer": "The project team is working hard."
                    },
                    {
                      "question": "Identify the abstract noun:",
                      "options": ["Computer", "Office", "Success", "Manager"],
                      "answer": "Success"
                    },
                    {
                      "question": "Select the correct plural form of 'focus':",
                      "options": ["focuses", "foci", "focusess", "both focuses and foci"],
                      "answer": "both focuses and foci"
                    },
                    {
                      "question": "What is the correct plural form of 'software'?",
                      "options": ["softwares", "software program", "software", "software apps"],
                      "answer": "software"
                    }
                  ],
                  "related_topics": [
                    {"title": "Pronouns"},
                    {"title": "Verbs"},
                    {"title": "Articles (A/An/The)"}
                  ]
                }
                """).build());
    }

    private void seedPronounsAndVerbs(List<Lesson> lessons) {
        // 3. Parts of Speech - Pronouns
        lessons.add(Lesson.builder().module("GRAMMAR").section("Parts of Speech").title("Pronouns").orderIndex(3)
                .content("""
                {
                  "difficulty": "Beginner",
                  "read_time": "10 min read",
                  "introduction": "### What is a Pronoun?\\n\\nA pronoun is a word used in place of a noun. It prevents the tedious repetition of names and items. For example, instead of saying: 'Rajesh likes coding, so Rajesh built Rajesh's own app,' you say: 'Rajesh likes coding, so he built his own app.'\\n\\nPronouns must align in gender, number, and case with the nouns they replace. Confusing pronoun cases is a common mistake in speech (e.g. saying 'Me and John will join' instead of 'John and I will join').",
                  "when_to_use": [
                    "Subject Pronouns (I, you, he, she, we, they) act as the subject performing actions.",
                    "Object Pronouns (me, you, him, her, us, them) receive actions.",
                    "Possessive Pronouns (mine, yours, his, hers, ours) demonstrate ownership.",
                    "Relative Pronouns (who, whom, which, that) link clauses."
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject Pronoun", "type": "primary"},
                      {"label": "Verb", "type": "secondary"},
                      {"label": "Object Pronoun", "type": "tertiary"}
                    ],
                    "note": "Always use 'I' in the subject position and 'me' in the object position."
                  },
                  "exceptions": [
                    {
                      "rule": "Use 'who' for people and 'which' for objects.",
                      "exception": "In informal contexts, 'that' is widely accepted for both people and objects.",
                      "example": "'He is the manager that helped me.'"
                    },
                    {
                      "rule": "Relative pronouns match the noun they replace.",
                      "exception": "When using collective nouns, the relative pronoun can shift based on whether the group acts as one body.",
                      "example": "'The committee which resolved the issue' vs 'The committee members who argued.'"
                    }
                  ],
                  "tables": [
                    {
                      "title": "Pronoun Formats in Workplace Contexts (15 Examples)",
                      "headers": ["Pronoun Case", "Word", "Context", "Workplace Sentence Example"],
                      "rows": [
                        ["Subject 1st Sg", "I", "Meeting", "I am going to share my screen now."],
                        ["Subject 3rd Masc", "he", "Casual", "He is the senior architect on our team."],
                        ["Subject 3rd Fem", "she", "Interview", "She handled the client calls single-handedly."],
                        ["Subject 1st Pl", "we", "Update", "We completed the database migration yesterday."],
                        ["Object 1st Sg", "me", "Email", "Please send the file to me directly."],
                        ["Object 3rd Masc", "him", "Meeting", "The director assigned him to lead the project."],
                        ["Object 3rd Fem", "her", "Casual", "I want to congratulate her on the promotion."],
                        ["Object 1st Pl", "us", "Update", "The team lead will guide us on this design."],
                        ["Possessive 1st Sg", "mine", "Desk", "The blue notebook is mine."],
                        ["Possessive 3rd Masc", "his", "Device", "That workstation on the right is his."],
                        ["Possessive 3rd Fem", "hers", "Proposal", "The best proposal was hers."],
                        ["Possessive 1st Pl", "ours", "Team", "The choice is ours to make."],
                        ["Relative Person", "who", "Interview", "She is the designer who built the UI."],
                        ["Relative Object", "which", "Report", "The bug which caused the crash is fixed."],
                        ["Reflexive Sg", "myself", "Introduction", "I created this presentation myself."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Self-Introduction in Interview",
                      "content": "I am a backend developer. I built the API infrastructure myself. My team members were supportive; they guided me when I encountered database issues. The project success was ours."
                    },
                    {
                      "context": "Professional Status Email",
                      "content": "Hi Team, Rajesh and I have reviewed the code. It looks clean. He will merge it today. Please reach out to him or me if you face any merge conflicts."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "Myself am Rajesh.",
                      "correct": "I am Rajesh. (or) My name is Rajesh.",
                      "why": "'Myself' is a reflexive pronoun; it cannot stand alone as the subject of a sentence."
                    },
                    {
                      "incorrect": "Me and my manager discussed the issue.",
                      "correct": "My manager and I discussed the issue.",
                      "why": "Use the subject pronoun 'I', and always place yourself second out of politeness."
                    },
                    {
                      "incorrect": "Between you and I, this is confidential.",
                      "correct": "Between you and me, this is confidential.",
                      "why": "'Between' is a preposition; it must be followed by object pronouns ('me', not 'I')."
                    },
                    {
                      "incorrect": "Each candidate must submit their resume.",
                      "correct": "Each candidate must submit his or her resume. (or) All candidates must submit their resumes.",
                      "why": "'Each' is singular, so it must pair with singular possessive pronouns like 'his or her'."
                    },
                    {
                      "incorrect": "She is the developer which I told you about.",
                      "correct": "She is the developer who I told you about.",
                      "why": "Use 'who' for people, not 'which'."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "What is the difference between 'who' and 'whom'?",
                      "answer": "Use 'who' in the subject position (performing action) and 'whom' in the object position (receiving action). Tip: If you can replace it with 'he', use 'who'. If you can replace it with 'him', use 'whom'."
                    },
                    {
                      "question": "Can I use 'their' as a singular pronoun?",
                      "answer": "Yes, 'they' and 'their' are widely accepted as singular gender-neutral pronouns in modern business English."
                    },
                    {
                      "question": "Is it correct to say 'taller than me'?",
                      "answer": "In formal settings, write 'taller than I' (which shortens 'taller than I am'). 'Taller than me' is common in casual conversation."
                    },
                    {
                      "question": "What is a relative pronoun?",
                      "answer": "A pronoun that connects a dependent clause to a noun (e.g., 'who', 'which', 'that', 'whose')."
                    },
                    {
                      "question": "When do I use 'its' vs 'it's'?",
                      "answer": "'Its' is a possessive pronoun showing ownership ('its screen'). 'It's' is a contraction of 'it is' ('it's raining')."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: The CTO sent the proposal to Rajesh and _____ (I/me) for comments.",
                      "type": "fill-in-blank",
                      "prefix": "The CTO sent the proposal to Rajesh and ",
                      "suffix": " for comments.",
                      "answer": "me"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Choose the correct sentence:",
                      "options": ["Between you and me, this decision is final.", "Between you and I, this decision is final.", "Between you and myself, this decision is final."],
                      "answer": "Between you and me, this decision is final."
                    },
                    {
                      "question": "Complete the sentence: 'She is the manager ___ approved the budget.'",
                      "options": ["which", "who", "whom", "whose"],
                      "answer": "who"
                    },
                    {
                      "question": "Choose the correct pronoun: 'We should ask ___ about the system crash.'",
                      "options": ["he", "him", "his"],
                      "answer": "him"
                    },
                    {
                      "question": "Identify the possessive pronoun:",
                      "options": ["Me", "Myself", "Ours", "We"],
                      "answer": "Ours"
                    },
                    {
                      "question": "What pronoun goes in: 'One should respect ___ colleagues.'",
                      "options": ["his", "their", "one's", "our"],
                      "answer": "one's"
                    }
                  ],
                  "related_topics": [
                    {"title": "Nouns"},
                    {"title": "Verbs"},
                    {"title": "Simple Present"}
                  ]
                }
                """).build());

        // 4. Parts of Speech - Verbs
        lessons.add(Lesson.builder().module("GRAMMAR").section("Parts of Speech").title("Verbs").orderIndex(4)
                .content("""
                {
                  "difficulty": "Beginner",
                  "read_time": "12 min read",
                  "introduction": "### The Power of Verbs\\n\\nVerbs express actions (build, run, design), states of being (is, exist, seem), or occurrences. Without a verb, a group of words is incomplete.\\n\\nUnderstanding how verbs conjugate is the gateway to master English tenses. Verbs can be action-oriented, stative (representing mental feelings), auxiliary (helping verbs), or transitive/intransitive.",
                  "when_to_use": [
                    "To describe active physical/mental processes ('He codes in Python.')",
                    "To express states of mind or emotion ('I appreciate your support.')",
                    "To build tense forms with helping verbs ('She is reviewing the documentation.')",
                    "To link subjects with descriptions ('The database is secure.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "Verb (conjugates by tense & subject)", "type": "secondary"},
                      {"label": "Object / Complement", "type": "tertiary"}
                    ],
                    "note": "Singular subjects require singular verbs (e.g. 'he works'); plural subjects take plural verbs ('they work')."
                  },
                  "exceptions": [
                    {
                      "rule": "Stative verbs do not take the continuous (-ing) form.",
                      "exception": "Some stative verbs shift meaning when used in the continuous form.",
                      "example": "'I think he is smart' (opinion) vs 'I am thinking about the offer' (mental process)."
                    },
                    {
                      "rule": "Regular past tense verbs end in '-ed'.",
                      "exception": "Irregular verbs have completely unique forms that must be memorized.",
                      "example": "'Go -> Went -> Gone' or 'Take -> Took -> Taken'."
                    }
                  ],
                  "tables": [
                    {
                      "title": "Verb Forms and Classifications (15 Examples)",
                      "headers": ["Verb Type", "Base (V1)", "Past (V2)", "Past Participle (V3)", "Workplace Context Sentence"],
                      "rows": [
                        ["Regular Action", "develop", "developed", "developed", "We developed a clean interface yesterday."],
                        ["Regular Action", "create", "created", "created", "She created the report layout last week."],
                        ["Regular Action", "manage", "managed", "managed", "He managed the migration project successfully."],
                        ["Irregular Action", "write", "wrote", "written", "I wrote the unit tests yesterday."],
                        ["Irregular Action", "build", "built", "built", "The architect built a robust architecture."],
                        ["Irregular Action", "speak", "spoke", "spoken", "I spoke to the client about the requirements."],
                        ["Irregular Action", "go", "went", "gone", "The engineer has gone to the data center."],
                        ["Auxiliary (Be)", "be", "was/were", "been", "She has been our consultant for a year."],
                        ["Auxiliary (Have)", "have", "had", "had", "We had completed the task before noon."],
                        ["Auxiliary (Do)", "do", "did", "done", "He did a wonderful job with the dashboard."],
                        ["Stative (Mental)", "know", "knew", "known", "I have known him for three years."],
                        ["Stative (Sense)", "see", "saw", "seen", "We saw the error log yesterday morning."],
                        ["Stative (Feel)", "like", "liked", "liked", "The customer liked our final interface."],
                        ["Stative (State)", "belong", "belonged", "belonged", "These credentials belong to the admin."],
                        ["Modal Helper", "should", "should", "should", "You should back up your database daily."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Answering Interview Question about Project Achievements",
                      "content": "In my last role, I designed the database schema. I optimized the queries, which reduced latency by 30%. I also led the migration team and coordinated weekly syncs with the stakeholders."
                    },
                    {
                      "context": "Incident Resolution Email",
                      "content": "The system crashed at 4 AM because the server memory filled up. We restarted the instance and verified all database connections. The application is running smoothly now."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "I am agree with you.",
                      "correct": "I agree with you.",
                      "why": "'Agree' is already a verb; do not couple it with 'am' in the simple present tense."
                    },
                    {
                      "incorrect": "He has went to the client location.",
                      "correct": "He has gone to the client location.",
                      "why": "Use the V3 past participle form ('gone') after has/have/had, never the V2 past form ('went')."
                    },
                    {
                      "incorrect": "The behavior of these systems are unstable.",
                      "correct": "The behavior of these systems is unstable.",
                      "why": "The subject is 'behavior' (singular), not 'systems' (plural). Align the verb as singular ('is')."
                    },
                    {
                      "incorrect": "I am knowing the credentials.",
                      "correct": "I know the credentials.",
                      "why": "'Know' is a stative verb. Do not use it in the continuous form."
                    },
                    {
                      "incorrect": "Neither Rajesh nor his colleagues was present.",
                      "correct": "Neither Rajesh nor his colleagues were present.",
                      "why": "With 'neither...nor', the verb agrees with the subject closest to it. 'Colleagues' is plural, so use 'were'."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "What is the difference between transitive and intransitive verbs?",
                      "answer": "Transitive verbs require an object to complete their meaning ('He wrote a report'). Intransitive verbs do not need an object ('She arrived')."
                    },
                    {
                      "question": "Can modal verbs change forms?",
                      "answer": "No, modal verbs like 'can', 'should', 'must' never add '-s' or '-ed'."
                    },
                    {
                      "question": "Is 'agree' a stative or active verb?",
                      "answer": "It is stative, describing a state of mind. Do not write 'I am agreeing'."
                    },
                    {
                      "question": "What are helping verbs?",
                      "answer": "Auxiliary verbs like 'be', 'do', and 'have' that assist the main verb in expressing tense or voice."
                    },
                    {
                      "question": "How do I irregular verbs form past tense?",
                      "answer": "Through vowel changes or entirely new words (e.g. eat -> ate). They do not follow the standard '+ed' rule."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Choose the correct verb: 'Every file on these disks _____ (is/are) scanned by the firewall.'",
                      "type": "fill-in-blank",
                      "prefix": "Every file on these disks ",
                      "suffix": " scanned by the firewall.",
                      "answer": "is"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Select the correct past participle (V3) of 'speak':",
                      "options": ["speaked", "spoke", "spoken", "speaking"],
                      "answer": "spoken"
                    },
                    {
                      "question": "Which of these is a stative verb?",
                      "options": ["build", "write", "understand", "deploy"],
                      "answer": "understand"
                    },
                    {
                      "question": "Identify the correct sentence:",
                      "options": ["I am agree with your proposal.", "I agree with your proposal.", "I am agreeing with your proposal."],
                      "answer": "I agree with your proposal."
                    },
                    {
                      "question": "Fill in the blank: 'Neither of the laptops ___ functional.'",
                      "options": ["is", "are", "were", "been"],
                      "answer": "is"
                    },
                    {
                      "question": "What is the V3 past participle of 'write'?",
                      "options": ["written", "wrote", "writed", "writing"],
                      "answer": "written"
                    }
                  ],
                  "related_topics": [
                    {"title": "Simple Present"},
                    {"title": "Present Continuous"},
                    {"title": "Modals"}
                  ]
                }
                """).build());
    }

    private void seedModifiersAndConnectors(List<Lesson> lessons) {
        // 5. Parts of Speech - Adjectives & Adverbs
        lessons.add(Lesson.builder().module("GRAMMAR").section("Parts of Speech").title("Adjectives & Adverbs").orderIndex(5)
                .content("""
                {
                  "difficulty": "Intermediate",
                  "read_time": "10 min read",
                  "introduction": "### Modifiers in Action\\n\\nAdjectives and adverbs describe and clarify other words in your sentences. An **adjective** modifies a noun or pronoun ('a successful launch'), while an **adverb** modifies a verb, adjective, or another adverb ('speaks very fluently').\\n\\nIn professional environments, using these modifiers correctly makes your feedback precise and constructive. Using adjectives instead of adverbs (like 'he writes code quick') is a common indicator of casual, unpolished speech.",
                  "when_to_use": [
                    "Use Adjectives directly before nouns to specify details ('clear layout').",
                    "Use Adjectives after linking verbs to describe subjects ('The database is secure.').",
                    "Use Adverbs of Manner to describe how an action is performed ('tests thoroughly').",
                    "Use Adverbs of Degree to modify the strength of a description ('highly efficient')."
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Adverb of Degree", "type": "primary"},
                      {"label": "Adjective", "type": "secondary"},
                      {"label": "Noun", "type": "tertiary"}
                    ],
                    "note": "Adjectives modify nouns; adverbs modify verbs, adjectives, or other adverbs."
                  },
                  "exceptions": [
                    {
                      "rule": "Adverbs of manner end in '-ly' (quick -> quickly).",
                      "exception": "Some words function as both adjectives and adverbs without changing form.",
                      "example": "'A fast developer' (adjective) vs 'He codes fast' (adverb). Never say 'fastly'."
                    },
                    {
                      "rule": "Linking verbs are modified by adverbs.",
                      "exception": "Linking verbs (feel, look, seem, sound) take adjectives rather than adverbs.",
                      "example": "'The design looks bad' (adjective) vs 'He designed it badly' (adverb)."
                    }
                  ],
                  "tables": [
                    {
                      "title": "Adjective vs. Adverb Parallel Mappings (15 Examples)",
                      "headers": ["Adjective (Modifies Noun)", "Adverb (Modifies Verb / Adj)", "Workplace Context Example"],
                      "rows": [
                        ["He is a quick developer.", "He codes quickly.", "We need to fix this quickly."],
                        ["She is a fluent presenter.", "She speaks fluently.", "She spoke fluently during the demo."],
                        ["This is a good report.", "The task was done well.", "You did the job well."],
                        ["The manager was quiet.", "He spoke quietly.", "We discussed the budget quietly."],
                        ["We had a late release.", "The engineer arrived late.", "He joined the call late."],
                        ["She is a careful coder.", "She reviews code carefully.", "Double-check the credentials carefully."],
                        ["This is a simple UI.", "The data is simply laid out.", "The page is simply structured."],
                        ["It was a heavy load.", "The server crashed heavily.", "The database was heavily loaded."],
                        ["The task was easy.", "We passed the test easily.", "We fixed the issue easily."],
                        ["The update was clear.", "He explained it clearly.", "Please state the objectives clearly."],
                        ["He is a hard worker.", "He works hard.", "He works hard on bug fixes."],
                        ["It was a sudden error.", "The system crashed suddenly.", "The connection dropped suddenly."],
                        ["This is a regular check.", "We back up data regularly.", "Backup files regularly."],
                        ["She has extreme focus.", "The system is extremely fast.", "This server is extremely stable."],
                        ["We made a prompt reply.", "Please respond promptly.", "The support team replied promptly."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Performance Feedback Email",
                      "content": "Hi Rajesh, you did a good job on the dashboard release. You worked hard and solved the database issues quickly. The final design looks extremely clean, and the interface loads fast."
                    },
                    {
                      "context": "Client Presentation Intro",
                      "content": "Our solution is highly scalable. We designed it carefully to ensure it handles heavy traffic easily. During testing, the system performed well under high loads."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "He speaks English very good.",
                      "correct": "He speaks English very well.",
                      "why": "Use the adverb 'well' to modify the verb 'speaks', not the adjective 'good'."
                    },
                    {
                      "incorrect": "She is more taller than her teammate.",
                      "correct": "She is taller than her teammate.",
                      "why": "Do not double-modify comparisons. Use 'taller', not 'more taller'."
                    },
                    {
                      "incorrect": "I felt badly about the server crash.",
                      "correct": "I felt bad about the server crash.",
                      "why": "Linking verbs like 'feel' require adjectives ('bad') rather than adverbs ('badly') to describe subject feelings."
                    },
                    {
                      "incorrect": "He works very hardly on this project.",
                      "correct": "He works very hard on this project.",
                      "why": "'Hard' is both adjective and adverb. 'Hardly' means 'almost not at all'."
                    },
                    {
                      "incorrect": "The server responded fastly.",
                      "correct": "The server responded fast.",
                      "why": "'Fastly' is not a word in standard English. 'Fast' functions as both adjective and adverb."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "When do I use 'good' vs 'well'?",
                      "answer": "Use 'good' as an adjective to describe a noun ('good programmer'). Use 'well' as an adverb to describe how an action is performed ('programmed well'). Exception: 'Well' can be an adjective when referring to health ('I feel well')."
                    },
                    {
                      "question": "Is 'friendly' an adjective or adverb?",
                      "answer": "It is an adjective. To use it as an adverb, say 'in a friendly manner' or 'friendly way'."
                    },
                    {
                      "question": "What is the difference between 'hard' and 'hardly'?",
                      "answer": "'Hard' means with energy or effort ('works hard'). 'Hardly' means almost not at all ('hardly works')."
                    },
                    {
                      "question": "How do I form the comparative of 'clear'?",
                      "answer": "Use 'clearer' for one-syllable adjectives. Do not write 'more clear' in formal contexts."
                    },
                    {
                      "question": "Where should adverbs of frequency go in a sentence?",
                      "answer": "Place them before the main verb ('always arrives') but after 'be' verbs ('is always late')."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: The support executive resolved the client's query _____ (efficient/efficiently).",
                      "type": "fill-in-blank",
                      "prefix": "The support executive resolved the client's query ",
                      "suffix": ".",
                      "answer": "efficiently"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Choose the correct adverb form: 'He drove ___ because of the rain.'",
                      "options": ["careful", "carefully", "more careful", "carefuller"],
                      "answer": "carefully"
                    },
                    {
                      "question": "Which of these is correct?",
                      "options": ["He runs very fastly.", "He runs very fast.", "He runs very quick."],
                      "answer": "He runs very fast."
                    },
                    {
                      "question": "What is the comparative form of 'bad'?",
                      "options": ["badder", "worst", "worse", "more bad"],
                      "answer": "worse"
                    },
                    {
                      "question": "Complete the sentence: 'The team did the job ___.'",
                      "options": ["good", "well", "fine", "excellent"],
                      "answer": "well"
                    },
                    {
                      "question": "Identify the adjective: 'The client was happy with the fast delivery.'",
                      "options": ["client", "happy", "delivery", "both happy and fast"],
                      "answer": "both happy and fast"
                    }
                  ],
                  "related_topics": [
                    {"title": "Verbs"},
                    {"title": "Prepositions & Conjunctions"},
                    {"title": "Articles (A/An/The)"}
                  ]
                }
                """).build());

        // 6. Parts of Speech - Prepositions & Conjunctions
        lessons.add(Lesson.builder().module("GRAMMAR").section("Parts of Speech").title("Prepositions & Conjunctions").orderIndex(6)
                .content("""
                {
                  "difficulty": "Intermediate",
                  "read_time": "12 min read",
                  "introduction": "### The Connectors of Language\\n\\nPrepositions and conjunctions serve as the structural glue of English sentences. A **preposition** defines spatial, temporal, or logical relationships between nouns ('on the desk', 'at 5 PM'). A **conjunction** links clauses or phrases, indicating addition, contrast, cause, or conditional dependencies ('and', 'but', 'because', 'although').\\n\\nIn corporate settings, master these small connectors prevents native-language translation errors (e.g. saying 'angry on me' instead of 'angry with me') and helps you write coherent emails.",
                  "when_to_use": [
                    "Use Prepositions of Time (in, on, at) to clarify calendars.",
                    "Use Prepositions of Place (in, on, at, under, behind) to specify locations.",
                    "Use Coordinating Conjunctions (FANBOYS) to connect equivalent clauses.",
                    "Use Subordinating Conjunctions (because, if, since) to establish dependency."
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Clause A", "type": "primary"},
                      {"label": "Conjunction", "type": "secondary"},
                      {"label": "Clause B", "type": "tertiary"}
                    ],
                    "note": "Prepositions link nouns to sentences; conjunctions link clauses to clauses."
                  },
                  "exceptions": [
                    {
                      "rule": "Use 'since' for starting points and 'for' for durations.",
                      "exception": "In informal speech, 'since' can occasionally mean 'because' without temporal connection.",
                      "example": "'Since you are free, could you review this code?'"
                    },
                    {
                      "rule": "Prepositions must precede their noun objects.",
                      "exception": "In informal questions, prepositions are frequently pushed to the end.",
                      "example": "'Who are you working with?' instead of 'With whom are you working?'"
                    }
                  ],
                  "tables": [
                    {
                      "title": "Prepositions and Conjunctions in Context (15 Examples)",
                      "headers": ["Type", "Connector", "Relationship", "Real-World Sentence Example"],
                      "rows": [
                        ["Preposition", "at", "Time", "The team sync starts at 9:30 AM."],
                        ["Preposition", "in", "Month", "The product launch is scheduled in December."],
                        ["Preposition", "on", "Day", "We submit status reports on Fridays."],
                        ["Preposition", "in", "Space", "The servers are located in Chennai."],
                        ["Preposition", "at", "Specific Point", "She is standing at the client counter."],
                        ["Preposition", "on", "Surface", "The hardware layout is on the board."],
                        ["Preposition", "for", "Duration", "He has worked on this backend for six months."],
                        ["Preposition", "since", "Start point", "We have used Oracle databases since 2018."],
                        ["Coordinating Conj", "but", "Contrast", "We tested the UI, but we missed a small layout bug."],
                        ["Coordinating Conj", "so", "Result", "The credentials expired, so we requested new ones."],
                        ["Coordinating Conj", "and", "Addition", "Rajesh wrote the logic and Amit verified the queries."],
                        ["Subordinating Conj", "although", "Concession", "Although the deadline was tight, we completed it on time."],
                        ["Subordinating Conj", "because", "Cause", "The API crashed because the server ran out of memory."],
                        ["Correlative Conj", "both...and", "Dual Addition", "He is both a Java expert and a system architect."],
                        ["Correlative Conj", "neither...nor", "Negative Choice", "Neither the manager nor the lead was there."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Professional Project Update",
                      "content": "Although the team faced server issues on Monday, we resolved them by Tuesday. Rajesh worked in the lab since morning, and because he fixed the code promptly, the deployment was successful."
                    },
                    {
                      "context": "Interview Answer describing Conflict Resolution",
                      "content": "I was angry with the decision to delay the release, but I discussed it calmly with my manager. We both looked at the data, and so we compromised on a partial launch."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "We will discuss about this project tomorrow.",
                      "correct": "We will discuss this project tomorrow.",
                      "why": "The verb 'discuss' is transitive and does not require the preposition 'about'."
                    },
                    {
                      "incorrect": "She was angry on Rajesh for the bug.",
                      "correct": "She was angry with Rajesh for the bug.",
                      "why": "The standard preposition to use with 'angry' when referring to a person is 'with', not 'on'."
                    },
                    {
                      "incorrect": "Although he tried hard, but he failed.",
                      "correct": "Although he tried hard, he failed. (or) He tried hard, but he failed.",
                      "why": "Using 'although' and 'but' together in a single sentence is redundant."
                    },
                    {
                      "incorrect": "I prefer Python than Java.",
                      "correct": "I prefer Python to Java.",
                      "why": "The verb 'prefer' takes the preposition 'to' when making comparisons, not 'than'."
                    },
                    {
                      "incorrect": "The office is open besides Sundays.",
                      "correct": "The office is open except Sundays. (or) beside Sundays.",
                      "why": "'Besides' means 'in addition to'. 'Except' means 'excluding'."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "What is the difference between 'between' and 'among'?",
                      "answer": "Use 'between' for two distinct entities ('between Rajesh and Amit'). Use 'among' for three or more entities in a group ('among the team members')."
                    },
                    {
                      "question": "Can a sentence end with a preposition?",
                      "answer": "Yes. While old rules advised against it, modern professional English accepts it ('Which project are you working on?')."
                    },
                    {
                      "question": "When do I use 'in' vs 'at' for locations?",
                      "answer": "Use 'in' for larger areas, cities, and enclosed spaces ('in Chennai', 'in the office'). Use 'at' for specific points, events, or addresses ('at the front desk', 'at the meeting')."
                    },
                    {
                      "question": "What are correlative conjunctions?",
                      "answer": "Conjunctions that work in pairs to connect elements, such as 'either...or', 'neither...nor', and 'not only...but also'."
                    },
                    {
                      "question": "Is 'because' always a conjunction?",
                      "answer": "Yes. However, when paired with 'of' ('because of'), it functions as a compound preposition."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: The developer has been working on this system _____ (since/for) two weeks.",
                      "type": "fill-in-blank",
                      "prefix": "The developer has been working on this system ",
                      "suffix": " two weeks.",
                      "answer": "for"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Choose the correct preposition: 'He will arrive ___ the office lobby.'",
                      "options": ["in", "on", "at", "for"],
                      "answer": "at"
                    },
                    {
                      "question": "Complete the sentence: 'We stayed up late ___ we had to resolve the API bug.'",
                      "options": ["but", "although", "because", "so"],
                      "answer": "because"
                    },
                    {
                      "question": "Which of these is incorrect?",
                      "options": ["Discuss the issue", "Discuss about the issue", "Talk about the issue"],
                      "answer": "Discuss about the issue"
                    },
                    {
                      "question": "Complete: 'Neither Rajesh ___ Amit was present at the sync.'",
                      "options": ["or", "nor", "and", "but"],
                      "answer": "nor"
                    },
                    {
                      "question": "Choose correct preposition: 'I prefer this IDE ___ the old one.'",
                      "options": ["than", "to", "over", "from"],
                      "answer": "to"
                    }
                  ],
                  "related_topics": [
                    {"title": "Adjectives & Adverbs"},
                    {"title": "Verbs"},
                    {"title": "Simple Present"}
                  ]
                }
                """).build());
    }

    private void seedArticlesAndPresentTenses(List<Lesson> lessons) {
        // 7. Articles (A/An/The)
        lessons.add(Lesson.builder().module("GRAMMAR").section("Articles & Determiners").title("Articles (A/An/The)").orderIndex(7)
                .content("""
                {
                  "difficulty": "Beginner",
                  "read_time": "10 min read",
                  "introduction": "### The Power of Small Words\\n\\nArticles (a, an, the) are determiners used to clarify whether a noun refers to something general/indefinite ('a project', 'an issue') or specific/definite ('the database', 'the CEO').\\n\\nCorrect article usage is critical in business writing. Dropping articles (e.g. saying 'we completed project' instead of 'we completed the project') makes communication look unpolished, while adding them where they don't belong (e.g. 'the Rajesh') indicates grammar confusion.",
                  "when_to_use": [
                    "Use 'A' before singular, countable nouns starting with consonant sounds ('a developer').",
                    "Use 'An' before singular, countable nouns starting with vowel sounds ('an hour').",
                    "Use 'The' to specify unique, specific, or previously mentioned nouns ('the server').",
                    "Use No Article (Zero Article) for general plurals, proper names, or general meals ('we like coffee')."
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Article (a/an/the)", "type": "primary"},
                      {"label": "Noun (singular or plural)", "type": "secondary"}
                    ],
                    "note": "'A' vs 'An' is determined by the starting sound of the word, not the starting letter."
                  },
                  "exceptions": [
                    {
                      "rule": "Words starting with 'u' take 'an' as it is a vowel.",
                      "exception": "If 'u' sounds like a consonant 'y' (/juː/), it takes 'a'.",
                      "example": "'A university student' or 'A unique concept'."
                    },
                    {
                      "rule": "Words starting with 'h' take 'a' as it is a consonant.",
                      "exception": "If 'h' is silent and the word starts with a vowel sound, it takes 'an'.",
                      "example": "'An hour late' or 'An honest review'."
                    }
                  ],
                  "tables": [
                    {
                      "title": "Article Determinations in Workplace Contexts (15 Examples)",
                      "headers": ["Article Type", "Word", "Rule / Sound Type", "Real-World Sentence Example"],
                      "rows": [
                        ["Indefinite", "a laptop", "Consonant sound", "I need to purchase a laptop today."],
                        ["Indefinite", "a user", "Consonant 'Y' sound", "We registered a new user on our site."],
                        ["Indefinite", "a manager", "Consonant sound", "She is looking for a manager position."],
                        ["Indefinite", "a database", "Consonant sound", "We need to set up a database schema."],
                        ["Indefinite", "an issue", "Vowel sound", "I encountered an issue during testing."],
                        ["Indefinite", "an hour", "Silent 'H' sound", "The meeting was delayed by an hour."],
                        ["Indefinite", "an update", "Vowel sound", "Please publish an update for the app."],
                        ["Indefinite", "an API", "Vowel sound (/eɪ/)", "We need to build an API wrapper."],
                        ["Definite", "the CTO", "Unique Position", "The CTO approved the design changes."],
                        ["Definite", "the server", "Specific item", "The server we configured has crashed."],
                        ["Definite", "the best", "Superlative", "This is the best code Rajesh has written."],
                        ["Definite", "the sun", "Unique entity", "The sun rises in the east."],
                        ["Zero Article", "(none)", "Proper Name", "Rajesh is the developer on call."],
                        ["Zero Article", "(none)", "General Meal", "We discussed the budget during lunch."],
                        ["Zero Article", "(none)", "General Plurals", "Computers are essential for programmers."]
                      ]
                    }
                  ],
                  "real_world_scenarios": [
                    {
                      "context": "Professional Bug Report",
                      "content": "I found an issue with the authentication API. The bug causes a delay of an hour when a user submits the request. I have informed the developer team."
                    },
                    {
                      "context": "Self-Introduction in Interview",
                      "content": "I graduated from a university in Chennai. I hold a degree in software engineering. In my last project, I was the lead designer for the mobile interface."
                    }
                  ],
                  "mistakes": [
                    {
                      "incorrect": "He is an university student.",
                      "correct": "He is a university student.",
                      "why": "'University' starts with a consonant /juː/ sound, so it takes 'a', not 'an'."
                    },
                    {
                      "incorrect": "I have been working here for a hour.",
                      "correct": "I have been working here for an hour.",
                      "why": "'Hour' starts with a silent 'h', presenting a vowel sound. Use 'an'."
                    },
                    {
                      "incorrect": "The honesty is the best policy.",
                      "correct": "Honesty is the best policy.",
                      "why": "Do not use 'the' before abstract nouns used in a general sense."
                    },
                    {
                      "incorrect": "We had the dinner together at 1 PM.",
                      "correct": "We had dinner together at 1 PM.",
                      "why": "General meals (breakfast, lunch, dinner) do not take articles unless referring to a specific event."
                    },
                    {
                      "incorrect": "The राजेश resolved the issue.",
                      "correct": "Rajesh resolved the issue.",
                      "why": "Never place articles before proper names of individuals in standard English."
                    }
                  ],
                  "faqs": [
                    {
                      "question": "Why does 'MP' take 'an' (an MP)?",
                      "answer": "The letter 'M' is pronounced with a starting vowel sound (/em/). Hence, abbreviations like 'MP' or 'MBA' take 'an'."
                    },
                    {
                      "question": "When do I use 'the' with country names?",
                      "answer": "Do not use 'the' for singular countries ('India'). Use 'the' if the country name contains plural elements or words like 'Republic', 'Kingdom', or 'States' ('the USA', 'the UK', 'the Netherlands')."
                    },
                    {
                      "question": "Can I say 'a software'?",
                      "answer": "No. 'Software' is uncountable. Use 'a software program' or 'an application'."
                    },
                    {
                      "question": "What is the difference between 'a few' and 'few'?",
                      "answer": "'A few' means some (positive connotation: 'I have a few ideas'). 'Few' means almost none (negative connotation: 'Few developers know this legacy system')."
                    },
                    {
                      "question": "Do I use articles before job titles?",
                      "answer": "Yes, use indefinite articles when introducing your profession ('I am a developer'). Use definite articles if you are referring to a single, unique title in that company ('He is the CEO')."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: We need to hire _____ (a/an) iOS developer for this project.",
                      "type": "fill-in-blank",
                      "prefix": "We need to hire ",
                      "suffix": " iOS developer for this project.",
                      "answer": "an"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Choose the correct article: 'She has ___ MBA degree from IIT.'",
                      "options": ["a", "an", "the", "no article"],
                      "answer": "an"
                    },
                    {
                      "question": "Complete the sentence: '___ information we received was outdated.'",
                      "options": ["A", "An", "The", "No article"],
                      "answer": "No article"
                    },
                    {
                      "question": "Correct the error: 'She is a one-eyed woman.'",
                      "options": ["She is an one-eyed woman.", "She is one-eyed woman.", "She is the one-eyed woman.", "No error"],
                      "answer": "No error"
                    }
                  ]
                }
                """).build());

        // 8. Simple Present
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Simple Present").orderIndex(8)
                .content("""
                {
                  "introduction": "The Simple Present Tense is one of the most frequently used tenses in the English language, forming the foundation for everyday communication. It is used to describe actions, states, and situations that are generally true, habitual, or permanent — things that don't change from moment to moment.\\n\\nUnlike the continuous tenses, which describe temporary or ongoing actions, the Simple Present captures routines, facts, and fixed schedules. When you say 'The sun rises in the east,' you're stating a permanent truth. When you say 'I work at a software company,' you're describing a general fact about your life, not something happening right this second.\\n\\nFor Indian English speakers preparing for interviews and placements, mastering the Simple Present is critical because it's the tense most commonly used in self-introductions, describing job responsibilities, and explaining daily routines — all frequent interview topics.\\n\\nThis lesson covers the complete structure of the Simple Present Tense, including its formula, correct usage in positive, negative, and question forms, common time expressions, and the mistakes Indian speakers most frequently make with this tense.",
                  "when_to_use": [
                    "Habitual or repeated actions ('She drinks coffee every morning.')",
                    "General truths and facts ('Water boils at 100°C.')",
                    "Permanent situations ('He lives in Chennai.')",
                    "Fixed schedules and timetables ('The train departs at 6 PM.')",
                    "Instructions and directions ('You turn left at the signal.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "Verb (base form / +s or +es)", "type": "secondary"},
                      {"label": "Object", "type": "tertiary"}
                    ],
                    "note": "Add '-s' or '-es' to the verb only when the subject is He, She, It, or a singular noun."
                  },
                  "tables": [
                    {
                      "title": "Positive Sentences (10 Examples)",
                      "headers": ["Subject", "Sentence"],
                      "rows": [
                        ["I", "I play cricket every weekend."],
                        ["You", "You speak English fluently."],
                        ["He", "He works at a bank."],
                        ["She", "She teaches mathematics."],
                        ["It", "It rains a lot in Coimbatore during monsoon."],
                        ["We", "We study together every evening."],
                        ["They", "They travel to Chennai every month."],
                        ["The company", "The company hires freshers every year."],
                        ["My father", "My father drives to office daily."],
                        ["The sun", "The sun rises in the east."]
                      ]
                    },
                    {
                      "title": "Negative Sentences (10 Examples)",
                      "headers": ["Subject", "Sentence"],
                      "rows": [
                        ["I", "I do not (don't) like spicy food."],
                        ["You", "You do not (don't) attend classes regularly."],
                        ["He", "He does not (doesn't) drink tea."],
                        ["She", "She does not (doesn't) watch TV at night."],
                        ["It", "It does not (doesn't) snow in Tamil Nadu."],
                        ["We", "We do not (don't) waste time."],
                        ["They", "They do not (don't) live in Coimbatore."],
                        ["The store", "The store does not (doesn't) open on Sundays."],
                        ["My brother", "My brother does not (doesn't) play football."],
                        ["The bus", "The bus does not (doesn't) stop here."]
                      ]
                    },
                    {
                      "title": "Question Form (10 Examples)",
                      "headers": ["Type", "Sentence"],
                      "rows": [
                        ["Yes/No", "Do you code in Java?"],
                        ["Yes/No", "Does he live in Chennai?"],
                        ["Yes/No", "Do they work on Saturdays?"],
                        ["Yes/No", "Does she speak English?"],
                        ["Yes/No", "Does the system work properly?"],
                        ["Wh- Question", "Where do you live?"],
                        ["Wh- Question", "What does he do for a living?"],
                        ["Wh- Question", "How do they travel to office?"],
                        ["Wh- Question", "Why does she want to learn Python?"],
                        ["Wh- Question", "When does the class start?"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "always", "usually", "often", "sometimes", "never", "every day", "on Mondays", "once a week"
                  ],
                  "tense_comparison": {
                    "title": "Simple Present vs Present Continuous",
                    "headers": ["Simple Present (Routine / Fact)", "Present Continuous (Right now / Temporary)"],
                    "rows": [
                      ["I work in Chennai. (Permanent job)", "I am working in Chennai this week. (Temporary)"],
                      ["The sun rises in the east. (General truth)", "Look! The sun is rising. (Happening right now)"],
                      ["She speaks English. (Ability/Fact)", "She is speaking English now. (Active speech)"]
                    ]
                  },
                  "mistakes": [
                    {
                      "incorrect": "He work in a software company.",
                      "correct": "He works in a software company.",
                      "why": "Third-person singular subjects (He, She, It) require the verb to end in '-s' or '-es' in the Simple Present."
                    },
                    {
                      "incorrect": "She don't like tea.",
                      "correct": "She doesn't like tea.",
                      "why": "Use 'does not' ('doesn't') for third-person singular, and 'do not' ('don't') for others."
                    },
                    {
                      "incorrect": "I am agree with you.",
                      "correct": "I agree with you.",
                      "why": "'Agree' is a verb, not an adjective. Do not use the helping verb 'am' before it in the Simple Present."
                    },
                    {
                      "incorrect": "I am working here since two years.",
                      "correct": "I have been working here for two years.",
                      "why": "Do not use present tenses to describe actions starting in the past and continuing now. Use Present Perfect Continuous."
                    },
                    {
                      "incorrect": "Does he plays cricket?",
                      "correct": "Does he play cricket?",
                      "why": "When using 'does' in questions, the main verb returns to its base form ('play'), dropping the '-s'."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: The sun _____ (set) in the west.",
                      "type": "fill-in-blank",
                      "prefix": "The sun ",
                      "suffix": " in the west.",
                      "answer": "sets"
                    },
                    {
                      "question": "Correct the error: 'They does not go to the gym.'",
                      "type": "error-spotting",
                      "sentence": "They does not go to the gym.",
                      "answer": "They do not go to the gym."
                    },
                    {
                      "question": "Select the correct verb: 'My brother _____ in Coimbatore.'",
                      "type": "mcq",
                      "options": ["live", "lives", "is live", "living"],
                      "answer": "lives"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Choose the correct sentence:",
                      "options": ["He speak English.", "He speaks English.", "He does speaks English.", "He is speak English."],
                      "answer": "He speaks English."
                    },
                    {
                      "question": "Identify the incorrect sentence:",
                      "options": ["Do you play football?", "Does she like coffee?", "Do they lives here?", "We do not code in Python."],
                      "answer": "Do they lives here?"
                    },
                    {
                      "question": "Choose correct: I ___ coffee every morning.",
                      "options": ["drink", "drinks"],
                      "answer": "drink"
                    },
                    {
                      "question": "Make negative: 'He plays football.'",
                      "options": ["He do not play football.", "He does not play football.", "He is not play football."],
                      "answer": "He does not play football."
                    },
                    {
                      "question": "Make question: 'She likes music.'",
                      "options": ["Do she like music?", "Does she likes music?", "Does she like music?"],
                      "answer": "Does she like music?"
                    }
                  ]
                }
                """).build());

        // 9. Present Continuous
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Present Continuous").orderIndex(9)
                .content("""
                {
                  "introduction": "The Present Continuous Tense describes actions that are happening at the exact moment of speaking, or ongoing actions that are temporary in nature. It is formed using the helping verb 'to be' (am, is, are) and the main verb with an '-ing' suffix.\\n\\nUnlike the Simple Present, which covers facts and habits, the Present Continuous captures the immediate present. When you say 'I am coding,' you mean you are typing code right this second.\\n\\nFor professional meetings, the Present Continuous is used to describe current project statuses, updates, and ongoing changes (e.g. 'We are developing a new feature.'). Understanding when to use it over the Simple Present is key to standard communication.",
                  "when_to_use": [
                    "Actions happening right now ('I am writing an email.')",
                    "Temporary situations ('He is staying in Bangalore for a month.')",
                    "Ongoing trends ('The inflation rate is rising.')",
                    "Fixed future arrangements ('We are launching the app tomorrow.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "am / is / are", "type": "secondary"},
                      {"label": "Verb + -ing", "type": "tertiary"}
                    ],
                    "note": "Use 'am' for I, 'is' for he/she/it, and 'are' for you/we/they."
                  },
                  "tables": [
                    {
                      "title": "Positive, Negative, and Question Examples (10 Examples)",
                      "headers": ["Type", "Sentence", "Subject/Context"],
                      "rows": [
                        ["Positive", "I am studying English now.", "I (happening now)"],
                        ["Positive", "She is calling the client.", "She (temporary task)"],
                        ["Positive", "We are launching a product.", "We (near future)"],
                        ["Negative", "I am not playing games.", "I (denial of current act)"],
                        ["Negative", "He is not working today.", "He (absence today)"],
                        ["Negative", "They are not coding in Java.", "They (using another language)"],
                        ["Question", "Are you listening to me?", "You (attention check)"],
                        ["Question", "Is it raining outside?", "It (weather check)"],
                        ["Question", "What are they doing?", "They (Wh- question)"],
                        ["Question", "Why is he arguing?", "He (Wh- reason)"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "now", "at the moment", "currently", "this week", "today", "right now", "these days"
                  ],
                  "tense_comparison": {
                    "title": "Simple Present vs Present Continuous",
                    "headers": ["Simple Present", "Present Continuous"],
                    "rows": [
                      ["I write reports. (regular task)", "I am writing a report now. (active now)"],
                      ["It rains in monsoon. (general truth)", "Look, it is raining now. (happening now)"]
                    ]
                  },
                  "mistakes": [
                    {
                      "incorrect": "I am working here since two years.",
                      "correct": "I have been working here for two years.",
                      "why": "Do not use Present Continuous for actions that started in the past and continue to the present; use Present Perfect Continuous instead."
                    },
                    {
                      "incorrect": "She is going to office daily.",
                      "correct": "She goes to office daily.",
                      "why": "Daily habits require Simple Present, not Present Continuous."
                    },
                    {
                      "incorrect": "He going to the market.",
                      "correct": "He is going to the market.",
                      "why": "Never drop the helping verb (am/is/are) in a continuous tense."
                    },
                    {
                      "incorrect": "I am knowing the manager.",
                      "correct": "I know the manager.",
                      "why": "Stative verbs like 'know' do not take the continuous form."
                    },
                    {
                      "incorrect": "What are you doing yesterday?",
                      "correct": "What were you doing yesterday?",
                      "why": "'Yesterday' requires the past continuous 'were', not the present continuous 'are'."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: Look! The birds _____ (fly) in the sky.",
                      "type": "fill-in-blank",
                      "prefix": "Look! The birds ",
                      "suffix": " in the sky.",
                      "answer": "are flying"
                    },
                    {
                      "question": "Correct the error: 'I am hearing a strange noise right now.'",
                      "type": "error-spotting",
                      "sentence": "I am hearing a strange noise right now.",
                      "answer": "I hear a strange noise right now."
                    },
                    {
                      "question": "Choose the correct continuous form: 'We _____ a new candidate this afternoon.'",
                      "type": "mcq",
                      "options": ["interview", "are interviewing", "is interviewing", "interviewed"],
                      "answer": "are interviewing"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'Why ___ you crying?'",
                      "options": ["is", "are", "am", "be"],
                      "answer": "are"
                    },
                    {
                      "question": "Choose correct: 'He ___ at a hotel this week.'",
                      "options": ["stay", "stays", "is staying", "stayed"],
                      "answer": "is staying"
                    },
                    {
                      "question": "Make negative: 'They are testing the code.'",
                      "options": ["They do not test the code.", "They are not testing the code.", "They not testing the code."],
                      "answer": "They are not testing the code."
                    },
                    {
                      "question": "Make question: 'She is learning Python.'",
                      "options": ["Is she learning Python?", "Does she learning Python?", "Are she learning Python?"],
                      "answer": "Is she learning Python?"
                    },
                    {
                      "question": "Correct the error: 'I am liking this movie.'",
                      "options": ["I like this movie.", "I am like this movie.", "I liked this movie."],
                      "answer": "I like this movie."
                    }
                  ]
                }
                """).build());

        // 9. Present Perfect
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Present Perfect").orderIndex(10)
                .content("""
                {
                  "introduction": "The Present Perfect Tense bridges the gap between the past and the present. It describes an action that occurred at an unspecified time in the past but has a direct impact or relevance to the present moment.\\n\\nIt is formed using the auxiliary verb 'has' or 'have' followed by the past participle (V3) form of the main verb. For example, saying 'I have completed my work' means the action happened in the past, but the result (being free now) is relevant today.\\n\\nIn professional communication, the Present Perfect is vital for sharing achievements, project updates, and experience. Understanding the distinction between the Present Perfect ('I have done it') and the Simple Past ('I did it yesterday') is crucial for professional clarity.",
                  "when_to_use": [
                    "Actions that happened at an indefinite time in the past ('I have visited London.')",
                    "Actions that started in the past and continue to the present ('She has worked here for five years.')",
                    "Recent actions with present results ('He has lost his keys.')",
                    "Life experiences ('We have tested many technologies.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "has / have", "type": "secondary"},
                      {"label": "Verb (V3 Past Participle)", "type": "tertiary"}
                    ],
                    "note": "Use 'has' for he/she/it/singular nouns, and 'have' for I/you/we/they/plural nouns."
                  },
                  "tables": [
                    {
                      "title": "Positive, Negative, and Question Examples (10 Examples)",
                      "headers": ["Type", "Sentence", "Subject/Context"],
                      "rows": [
                        ["Positive", "I have finished the project.", "I (completed act)"],
                        ["Positive", "She has lived here for a year.", "She (ongoing state)"],
                        ["Positive", "They have already eaten.", "They (recent act)"],
                        ["Negative", "I have not seen the movie.", "I (experience gap)"],
                        ["Negative", "He has not replied yet.", "He (pending response)"],
                        ["Negative", "We have not started the meeting.", "We (status update)"],
                        ["Question", "Have you submitted the code?", "You (yes/no check)"],
                        ["Question", "Has she called you?", "She (yes/no check)"],
                        ["Question", "Where have they gone?", "They (Wh- location)"],
                        ["Question", "How long has he been here?", "He (Wh- duration)"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "already", "yet", "just", "ever", "never", "since", "for", "recently", "so far", "up to now"
                  ],
                  "tense_comparison": {
                    "title": "Present Perfect vs Simple Past",
                    "headers": ["Present Perfect (Indefinite time/Present result)", "Simple Past (Definite time/Past action)"],
                    "rows": [
                      ["I have sent the email. (relevant now)", "I sent the email yesterday. (finished event)"],
                      ["She has lived in Chennai for years. (still lives there)", "She lived in Chennai for years. (no longer lives there)"]
                    ]
                  },
                  "mistakes": [
                    {
                      "incorrect": "I have completed my graduation in 2024.",
                      "correct": "I completed my graduation in 2024.",
                      "why": "Do not use the Present Perfect tense when a specific past time ('in 2024') is mentioned; use Simple Past instead."
                    },
                    {
                      "incorrect": "She did not replied to my email yet.",
                      "correct": "She has not replied to my email yet.",
                      "why": "The time marker 'yet' is typically used with the Present Perfect tense, not Simple Past."
                    },
                    {
                      "incorrect": "He has wrote a detailed report.",
                      "correct": "He has written a detailed report.",
                      "why": "Always use the V3 past participle form ('written') after has/have/had, not the V2 past form ('wrote')."
                    },
                    {
                      "incorrect": "I have been knowing him for a long time.",
                      "correct": "I have known him for a long time.",
                      "why": "'Know' is a stative verb; use Present Perfect instead of Present Perfect Continuous."
                    },
                    {
                      "incorrect": "Has they finished the audit?",
                      "correct": "Have they finished the audit?",
                      "why": "Use 'have' with plural subjects ('they'), not 'has'."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: We _____ (know) each other since our college days.",
                      "type": "fill-in-blank",
                      "prefix": "We ",
                      "suffix": " each other since our college days.",
                      "answer": "have known"
                    },
                    {
                      "question": "Correct the error: 'I saw that movie already.'",
                      "type": "error-spotting",
                      "sentence": "I saw that movie already.",
                      "answer": "I have seen that movie already."
                    },
                    {
                      "question": "Choose the correct tense form: 'She _____ her breakfast just now.'",
                      "type": "mcq",
                      "options": ["has had", "had", "have had", "was having"],
                      "answer": "has had"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: '___ you ever been to Delhi?'",
                      "options": ["Has", "Have", "Did", "Were"],
                      "answer": "Have"
                    },
                    {
                      "question": "Choose correct: 'He has not finished the work ___.'",
                      "options": ["already", "yet", "since", "ago"],
                      "answer": "yet"
                    },
                    {
                      "question": "Make negative: 'She has returned.'",
                      "options": ["She did not return.", "She has not returned.", "She is not returned."],
                      "answer": "She has not returned."
                    },
                    {
                      "question": "Make question: 'They have finished the test.'",
                      "options": ["Have they finished the test?", "Has they finished the test?", "Did they finished the test?"],
                      "answer": "Have they finished the test?"
                    },
                    {
                      "question": "Correct the error: 'I have seen him two days ago.'",
                      "options": ["I saw him two days ago.", "I have saw him two days ago.", "I was seeing him two days ago."],
                      "answer": "I saw him two days ago."
                    }
                  ]
                }
                """).build());

        // 10. Simple Past
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Simple Past").orderIndex(11)
                .content("""
                {
                  "introduction": "The Simple Past Tense is used to describe actions, states, or events that were completed at a definite time in the past. It is the core narrative tense in English, used whenever you talk about finished history.\\n\\nIt is formed by using the past form of the verb (V2). Regular verbs add '-ed' (work -> worked), while irregular verbs take unique forms (buy -> bought, speak -> spoke) that must be memorized.\\n\\nFor interview narratives, telling stories about previous achievements, or writing incident reports, the Simple Past is the most critical tense. It cleanly demarcates past events from current states.",
                  "when_to_use": [
                    "Completed actions in the past ('We launched the website yesterday.')",
                    "Past habits or repeated actions ('I played tennis every day during school.')",
                    "Past states of being ('He was the manager at that time.')",
                    "A series of finished past actions ('He entered the room, sat down, and started coding.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "Verb (V2 Past Form)", "type": "secondary"},
                      {"label": "Object / Complement", "type": "tertiary"}
                    ],
                    "note": "Negative sentences and questions use the auxiliary verb 'did' + the base form (V1) of the verb."
                  },
                  "tables": [
                    {
                      "title": "Positive, Negative, and Question Examples (10 Examples)",
                      "headers": ["Type", "Sentence", "Subject/Verb Style"],
                      "rows": [
                        ["Positive", "I worked in Bangalore last year.", "I (regular verb)"],
                        ["Positive", "She wrote an elegant script.", "She (irregular verb)"],
                        ["Positive", "They went to the market.", "They (irregular verb)"],
                        ["Negative", "I did not (didn't) like the food.", "I (did not + base verb)"],
                        ["Negative", "He did not (didn't) answer the call.", "He (did not + base verb)"],
                        ["Negative", "We didn't see the updates.", "We (did not + base verb)"],
                        ["Question", "Did you submit the report?", "You (Did + subject + base verb)"],
                        ["Question", "Did she attend the meeting?", "She (Did + subject + base verb)"],
                        ["Question", "Where did they go?", "They (Wh- location)"],
                        ["Question", "When did the train depart?", "It (Wh- time)"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "yesterday", "ago", "last week", "last year", "in 2023", "the other day", "when I was young"
                  ],
                  "tense_comparison": {
                    "title": "Simple Past vs Past Continuous",
                    "headers": ["Simple Past (Completed action)", "Past Continuous (Action in progress in the past)"],
                    "rows": [
                      ["I ate dinner. (finished eating)", "I was eating dinner when you called. (ongoing in past)"],
                      ["She coded the module. (completed task)", "She was coding when the power cut occurred. (interrupted action)"]
                    ]
                  },
                  "mistakes": [
                    {
                      "incorrect": "I did not went to office yesterday.",
                      "correct": "I did not go to office yesterday.",
                      "why": "After the auxiliary 'did' / 'did not', always use the base form (V1) of the verb ('go', not 'went')."
                    },
                    {
                      "incorrect": "Did you saw the news?",
                      "correct": "Did you see the news?",
                      "why": "Questions starting with 'Did' must use the base form (V1) of the main verb ('see', not 'saw')."
                    },
                    {
                      "incorrect": "I have met him yesterday.",
                      "correct": "I met him yesterday.",
                      "why": "The time marker 'yesterday' specifies a definite past time, which requires Simple Past, not Present Perfect."
                    },
                    {
                      "incorrect": "She was write a letter when I entered.",
                      "correct": "She was writing a letter when I entered.",
                      "why": "Past continuous requires the helper 'was' + 'writing' (verb-ing), not base verb."
                    },
                    {
                      "incorrect": "He tell me that he completed the task.",
                      "correct": "He told me that he had completed the task.",
                      "why": "Use past tense 'told' to match the past time frame, and Past Perfect for the action that happened first."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: She _____ (buy) a new laptop last Friday.",
                      "type": "fill-in-blank",
                      "prefix": "She ",
                      "suffix": " a new laptop last Friday.",
                      "answer": "bought"
                    },
                    {
                      "question": "Correct the error: 'I didn't received the parcel yet.'",
                      "type": "error-spotting",
                      "sentence": "I didn't received the parcel yet.",
                      "answer": "I haven't received the parcel yet."
                    },
                    {
                      "question": "Select the correct form: 'The manager _____ us to submit the reports early.'",
                      "type": "mcq",
                      "options": ["ask", "asked", "asking", "had ask"],
                      "answer": "asked"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'Where ___ you go last Sunday?'",
                      "options": ["do", "did", "were", "have"],
                      "answer": "did"
                    },
                    {
                      "question": "Choose correct: 'The light went out while I ___.'",
                      "options": ["read", "was reading", "had read", "reading"],
                      "answer": "was reading"
                    },
                    {
                      "question": "Make negative: 'She sang a song.'",
                      "options": ["She did not sang a song.", "She did not sing a song.", "She was not sing a song."],
                      "answer": "She did not sing a song."
                    },
                    {
                      "question": "Make question: 'He broke the vase.'",
                      "options": ["Did he broke the vase?", "Did he break the vase?", "Was he break the vase?"],
                      "answer": "Did he break the vase?"
                    },
                    {
                      "question": "Correct the error: 'I had visited Agra in 2020.'",
                      "options": ["I visited Agra in 2020.", "I have visited Agra in 2020.", "I was visiting Agra in 2020."],
                      "answer": "I visited Agra in 2020."
                    }
                  ]
                }
                """).build());

        // 11. Past Continuous
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Past Continuous").orderIndex(12)
                .content("""
                {
                  "introduction": "The Past Continuous Tense is used to describe actions that were ongoing or in progress at a specific moment in the past. It highlights the duration and active state of the past action, rather than just its completion.\\n\\nIt is formed using the past helping verbs 'was' or 'were' combined with the main verb ending in '-ing'. For example, 'I was coding at 10 PM yesterday.'\\n\\nIn professional settings, it is commonly used to provide context for interruptions or explain what was happening when an incident occurred (e.g. 'The server crashed while we were runnning tests.'). Understanding this tense adds narrative depth to your speech.",
                  "when_to_use": [
                    "An action in progress at a specific time in the past ('At noon, I was eating lunch.')",
                    "An ongoing past action interrupted by a shorter action ('I was writing code when the alarm rang.')",
                    "Two actions happening simultaneously in the past ('While I was coding, my colleague was designing.')",
                    "Setting the atmosphere in a past narrative ('The sun was shining, and employees were chatting.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "was / were", "type": "secondary"},
                      {"label": "Verb + -ing", "type": "tertiary"}
                    ],
                    "note": "Use 'was' for singular subjects (I, he, she, it) and 'were' for plural subjects (you, we, they)."
                  },
                  "tables": [
                    {
                      "title": "Positive, Negative, and Question Examples (10 Examples)",
                      "headers": ["Type", "Sentence", "Subject/Context"],
                      "rows": [
                        ["Positive", "I was debugging the server.", "I (singular activity)"],
                        ["Positive", "She was preparing the slides.", "She (singular activity)"],
                        ["Positive", "They were discussing the budget.", "They (plural activity)"],
                        ["Negative", "I was not sleeping at that time.", "I (denial of state)"],
                        ["Negative", "He was not listening to the call.", "He (absence of focus)"],
                        ["Negative", "We were not testing the database.", "We (denial of task)"],
                        ["Question", "Were you working yesterday?", "You (yes/no check)"],
                        ["Question", "Was it snowing in Kashmir?", "It (weather check)"],
                        ["Question", "What were they doing at 8 PM?", "They (Wh- question)"],
                        ["Question", "Why was he running late?", "He (Wh- reason)"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "while", "when", "at that time", "yesterday evening", "at 5 PM yesterday", "all day yesterday"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "They was working on the project.",
                      "correct": "They were working on the project.",
                      "why": "Plural subjects like 'they' require the past helping verb 'were', not 'was'."
                    },
                    {
                      "incorrect": "I was write code when you called.",
                      "correct": "I was writing code when you called.",
                      "why": "The past continuous requires 'was' + 'writing' (verb-ing), not the base form."
                    },
                    {
                      "incorrect": "We were coding since five hours.",
                      "correct": "We had been coding for five hours.",
                      "why": "For actions indicating duration up to a point in the past, use Past Perfect Continuous, not Past Continuous."
                    },
                    {
                      "incorrect": "While I cooked dinner, the phone rang.",
                      "correct": "While I was cooking dinner, the phone rang.",
                      "why": "Use the continuous form ('was cooking') to show the background action that was interrupted."
                    },
                    {
                      "incorrect": "I was knowing the truth back then.",
                      "correct": "I knew the truth back then.",
                      "why": "'Know' is a stative verb and should not be used in the past continuous form."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: While the team _____ (discuss) the plan, the power cut occurred.",
                      "type": "fill-in-blank",
                      "prefix": "While the team ",
                      "suffix": " the plan, the power cut occurred.",
                      "answer": "was discussing"
                    },
                    {
                      "question": "Correct the error: 'I was work on the computer all day yesterday.'",
                      "type": "error-spotting",
                      "sentence": "I was work on the computer all day yesterday.",
                      "answer": "I was working on the computer all day yesterday."
                    },
                    {
                      "question": "Select the correct combination: 'We _____ when the manager _____ the room.'",
                      "type": "mcq",
                      "options": ["coded / entered", "were coding / entered", "were coding / was entering", "coded / was entering"],
                      "answer": "were coding / entered"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'They ___ playing chess when the guest arrived.'",
                      "options": ["was", "were", "are", "been"],
                      "answer": "were"
                    },
                    {
                      "question": "Choose correct: 'I was sleeping ___ the bell rang.'",
                      "options": ["while", "when", "during", "since"],
                      "answer": "when"
                    },
                    {
                      "question": "Make negative: 'She was laughing.'",
                      "options": ["She did not laughing.", "She was not laughing.", "She not was laughing."],
                      "answer": "She was not laughing."
                    },
                    {
                      "question": "Make question: 'He was writing a test.'",
                      "options": ["Was he writing a test?", "Were he writing a test?", "Did he writing a test?"],
                      "answer": "Was he writing a test?"
                    },
                    {
                      "question": "Correct the error: 'He was loving his job back then.'",
                      "options": ["He loved his job back then.", "He was love his job back then.", "He has loved his job."],
                      "answer": "He loved his job back then."
                    }
                  ]
                }
                """).build());

        // 12. Past Perfect
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Past Perfect").orderIndex(13)
                .content("""
                {
                  "introduction": "The Past Perfect Tense is used to talk about an action that was completed before another action in the past. It represents the 'past of the past' and is used to clarify the sequence of events.\\n\\nIt is formed using the helping verb 'had' followed by the past participle (V3) form of the main verb. For example, 'The train had left when I reached the station.' (Action 1: train left; Action 2: I reached).\\n\\nIn professional environments, this tense is highly useful for explaining timelines, dependency issues, and resolutions (e.g. 'We noticed that the developer had committed the wrong code.'). Mastering this prevents confusion about chronological sequences.",
                  "when_to_use": [
                    "An action completed before another action in the past ('She had completed the task before the meeting started.')",
                    "An action completed before a specific past time ('By 5 PM, they had finished the audit.')",
                    "Conditional sentences expressing past regrets ('If I had studied harder, I would have passed.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "had", "type": "secondary"},
                      {"label": "Verb (V3 Past Participle)", "type": "tertiary"}
                    ],
                    "note": "'Had' is used universally for all subjects (I, he, she, it, we, you, they)."
                  },
                  "tables": [
                    {
                      "title": "Positive, Negative, and Question Examples (10 Examples)",
                      "headers": ["Type", "Sentence", "Chronology (Action 1 / Action 2)"],
                      "rows": [
                        ["Positive", "I had submitted the report before the deadline.", "Action 1: submitted report / Action 2: deadline reached"],
                        ["Positive", "She had left when I arrived.", "Action 1: she left / Action 2: I arrived"],
                        ["Positive", "They had resolved the bug by noon.", "Action 1: resolved bug / Action 2: noon arrived"],
                        ["Negative", "I had not seen the documentation.", "Action 1: did not see doc / Action 2: read code"],
                        ["Negative", "He had not replied before the escalation.", "Action 1: did not reply / Action 2: escalation occurred"],
                        ["Negative", "We hadn't tested the backup server.", "Action 1: did not test backup / Action 2: main crashed"],
                        ["Question", "Had you read the terms before signing?", "Action 1: read terms / Action 2: signed contract"],
                        ["Question", "Had they launched the app by last month?", "Action 1: launched app / Action 2: last month passed"],
                        ["Question", "What had she done before you called?", "Action 1: she did something / Action 2: you called"],
                        ["Question", "Why had he resigned before the promotion?", "Action 1: he resigned / Action 2: promotion announced"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "by the time", "before", "after", "already", "by 2023", "by noon", "never...before"
                  ],
                  "tense_comparison": {
                    "title": "Past Perfect vs Simple Past",
                    "headers": ["Past Perfect (First past action)", "Simple Past (Second past action)"],
                    "rows": [
                      ["The meeting had started. (happened first)", "The manager entered the room. (happened second)"],
                      ["He had completed the draft. (happened first)", "He submitted it. (happened second)"]
                    ]
                  },
                  "mistakes": [
                    {
                      "incorrect": "The movie started before we reached the theater.",
                      "correct": "The movie had started before we reached the theater.",
                      "why": "Since the movie started first, use Past Perfect ('had started') to show it preceded reaching the theater."
                    },
                    {
                      "incorrect": "I had visited Delhi last year.",
                      "correct": "I visited Delhi last year.",
                      "why": "Do not use Past Perfect for a single, isolated past action with a specific time; use Simple Past."
                    },
                    {
                      "incorrect": "He had wrote the email before I asked.",
                      "correct": "He had written the email before I asked.",
                      "why": "Always use the past participle V3 form ('written') after 'had', not V2 past form ('wrote')."
                    },
                    {
                      "incorrect": "If I saw the sign, I would have stopped.",
                      "correct": "If I had seen the sign, I would have stopped.",
                      "why": "Third conditional structures require Past Perfect ('had seen') in the 'if' clause."
                    },
                    {
                      "incorrect": "After he finished the report, he had submitted it.",
                      "correct": "After he had finished the report, he submitted it.",
                      "why": "The first action (finishing the report) takes Past Perfect, and the second action (submitting it) takes Simple Past."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: By the time the doctor arrived, the patient _____ (die).",
                      "type": "fill-in-blank",
                      "prefix": "By the time the doctor arrived, the patient ",
                      "suffix": ".",
                      "answer": "had died"
                    },
                    {
                      "question": "Correct the error: 'She said she didn't saw him before.'",
                      "type": "error-spotting",
                      "sentence": "She said she didn't saw him before.",
                      "answer": "She said she hadn't seen him before."
                    },
                    {
                      "question": "Select the correct combination: 'After we _____ the code, we _____ the application.'",
                      "type": "mcq",
                      "options": ["tested / deployed", "had tested / deployed", "tested / had deployed", "had tested / had deployed"],
                      "answer": "had tested / deployed"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'He left because he ___ finished his shift.'",
                      "options": ["has", "had", "was", "did"],
                      "answer": "had"
                    },
                    {
                      "question": "Choose correct: 'By 10 PM yesterday, we ___ the audit.'",
                      "options": ["completed", "were completing", "had completed", "have completed"],
                      "answer": "had completed"
                    },
                    {
                      "question": "Make negative: 'She had slept.'",
                      "options": ["She had not slept.", "She has not slept.", "She didn't slept."],
                      "answer": "She had not slept."
                    },
                    {
                      "question": "Make question: 'He had read the book.'",
                      "options": ["Had he read the book?", "Did he read the book?", "Has he read the book?"],
                      "answer": "Had he read the book?"
                    },
                    {
                      "question": "Correct the error: 'If I worked harder, I would have cleared.'",
                      "options": ["If I had worked harder, I would have cleared.", "If I have worked harder, I would have cleared.", "If I would work harder, I cleared."],
                      "answer": "If I had worked harder, I would have cleared."
                    }
                  ]
                }
                """).build());

        // 13. Future Tenses
        lessons.add(Lesson.builder().module("GRAMMAR").section("Tenses").title("Future Tenses").orderIndex(14)
                .content("""
                {
                  "introduction": "Future tenses describe actions, states, or events that will occur at some point after the present moment. In professional settings, future tenses are highly active, used for planning, forecasting, making offers, and sharing goals.\\n\\nThe future is expressed in several ways: using 'will' (for spontaneous decisions, promises, predictions), 'be going to' (for plans/intentions), or the Present Continuous (for scheduled events). There is also the Future Continuous ('I will be working') and Future Perfect ('I will have finished').\\n\\nMastering these variations allows you to speak about future obligations, timelines, and commitments with absolute precision. This lesson covers the primary future structures, their guidelines, and common mistakes.",
                  "when_to_use": [
                    "Will: spontaneous choices, promises, predictions ('I will help you with this.')",
                    "Be going to: prior intentions or plans ('We are going to launch the beta next week.')",
                    "Future Continuous: actions in progress in the future ('I will be presenting at 10 AM tomorrow.')",
                    "Future Perfect: actions completed before a future point ('We will have deployed by Friday.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "will / be going to", "type": "secondary"},
                      {"label": "Verb (base form)", "type": "tertiary"}
                    ],
                    "note": "'Will' is followed by the base form V1. Do not use 'to' after will (e.g. 'I will go', not 'I will to go')."
                  },
                  "tables": [
                    {
                      "title": "Future Tense Forms and Examples (10 Examples)",
                      "headers": ["Tense Form", "Structure", "Example Sentence"],
                      "rows": [
                        ["Simple Future (Will)", "will + V1", "I will call you when I arrive."],
                        ["Simple Future (Will)", "will + V1", "The company will hire freshers soon."],
                        ["Simple Future (Going to)", "be going to + V1", "I am going to upgrade my skills."],
                        ["Simple Future (Going to)", "be going to + V1", "They are going to shift the office."],
                        ["Future Continuous", "will be + V-ing", "I will be coding all night tomorrow."],
                        ["Future Continuous", "will be + V-ing", "She will be traveling to Mumbai next week."],
                        ["Future Perfect", "will have + V3", "We will have finished the module by Friday."],
                        ["Future Perfect", "will have + V3", "He will have retired by next year."],
                        ["Negative (Will)", "will not (won't) + V1", "They won't attend the team lunch."],
                        ["Question (Will)", "Will + subject + V1?", "Will you review my code tomorrow?"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "tomorrow", "next week", "in two days", "by Friday", "by next year", "soon", "in the future"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "I will go to the office when the rain will stop.",
                      "correct": "I will go to the office when the rain stops.",
                      "why": "In time clauses (starting with 'when', 'before', 'after', 'until'), use the Simple Present to refer to the future."
                    },
                    {
                      "incorrect": "I will to submit the document by tomorrow.",
                      "correct": "I will submit the document by tomorrow.",
                      "why": "Do not place the infinitive indicator 'to' after modal verbs like 'will'."
                    },
                    {
                      "incorrect": "We will have completed the project next Friday.",
                      "correct": "We will have completed the project by next Friday. (or) We will complete it next Friday.",
                      "why": "The Future Perfect ('will have completed') requires the time marker 'by' (not 'on' or 'next') to denote completion before that limit."
                    },
                    {
                      "incorrect": "I think I am going to win the match.",
                      "correct": "I think I will win the match.",
                      "why": "Use 'will' for predictions and opinions based on thoughts ('I think'), rather than 'going to'."
                    },
                    {
                      "incorrect": "He won't to cooperate with us.",
                      "correct": "He won't cooperate with us.",
                      "why": "'Won't' is the contraction of 'will not'; it must be followed directly by base V1, without 'to'."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank: If you help me, I _____ (finish) the task sooner.",
                      "type": "fill-in-blank",
                      "prefix": "If you help me, I ",
                      "suffix": " the task sooner.",
                      "answer": "will finish"
                    },
                    {
                      "question": "Correct the error: 'By next year, he will have working here for a decade.'",
                      "type": "error-spotting",
                      "sentence": "By next year, he will have working here for a decade.",
                      "answer": "By next year, he will have been working here for a decade. (or) will have worked"
                    },
                    {
                      "question": "Choose the correct form for an event scheduled on a calendar: 'The train _____ at 8 AM tomorrow.'",
                      "type": "mcq",
                      "options": ["leaves", "will leave", "is leaving", "will have left"],
                      "answer": "leaves"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'I ___ visit my parents next month.' (have planned it)",
                      "options": ["will", "am going to", "would", "shall"],
                      "answer": "am going to"
                    },
                    {
                      "question": "Choose correct: 'We will start the test as soon as he ___.'",
                      "options": ["will arrive", "arrives", "arrived", "will have arrived"],
                      "answer": "arrives"
                    },
                    {
                      "question": "Make negative: 'I will help you.'",
                      "options": ["I will not help you.", "I not will help you.", "I don't will help you."],
                      "answer": "I will not help you."
                    },
                    {
                      "question": "Make question: 'She will join us.'",
                      "options": ["Will she join us?", "Does she will join us?", "Is she will join us?"],
                      "answer": "Will she join us?"
                    },
                    {
                      "question": "Correct the error: 'By next Monday, we will have submit the audit.'",
                      "options": ["By next Monday, we will have submitted the audit.", "By next Monday, we will submit the audit.", "By next Monday, we will have submitting the audit."],
                      "answer": "By next Monday, we will have submitted the audit."
                    }
                  ]
                }
                """).build());
    }

    private void seedVoiceSpeechConditionalsModals(List<Lesson> lessons) {
        // 14. Active & Passive Voice
        lessons.add(Lesson.builder().module("GRAMMAR").section("Active & Passive Voice").title("Active & Passive Voice").orderIndex(15)
                .content("""
                {
                  "introduction": "In English grammar, voice represents the relationship between the action expressed by the verb and the participants identified by its arguments (subject, object).\\n\\nIn the **Active Voice**, the subject performs the action (e.g., 'The developer wrote the code.'). In the **Passive Voice**, the subject receives the action (e.g., 'The code was written by the developer.').\\n\\nIn professional writing, passive voice is highly useful for emphasizing the result rather than the actor, especially in incident reports, scientific findings, and formal notices (e.g., 'The server was updated at midnight'). However, active voice is preferred for clear, direct business communication. This lesson covers active-to-passive conversions, tense shifts, and correct professional use cases.",
                  "when_to_use": [
                    "Active: Use when the performer of the action is important, clear, or direct ('Our team completed the audit.')",
                    "Passive: Use when the performer is unknown, obvious, or less important than the action itself ('The files were deleted.')",
                    "Passive: Use for formal warnings or policies to sound objective ('Payment is required upon arrival.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Object as Subject", "type": "primary"},
                      {"label": "be-verb + Past Participle (V3)", "type": "secondary"},
                      {"label": "by + Original Subject", "type": "tertiary"}
                    ],
                    "note": "Only transitive verbs (verbs that take objects) can be converted into the passive voice."
                  },
                  "tables": [
                    {
                      "title": "Active vs. Passive Tense Conversions (10 Examples)",
                      "headers": ["Tense", "Active Sentence", "Passive Sentence Equivalent"],
                      "rows": [
                        ["Simple Present", "The developer writes the code.", "The code is written by the developer."],
                        ["Present Continuous", "The developer is writing the code.", "The code is being written by the developer."],
                        ["Present Perfect", "The developer has written the code.", "The code has been written by the developer."],
                        ["Simple Past", "The developer wrote the code.", "The code was written by the developer."],
                        ["Past Continuous", "The developer was writing the code.", "The code was being written by the developer."],
                        ["Past Perfect", "The developer had written the code.", "The code had been written by the developer."],
                        ["Simple Future", "The developer will write the code.", "The code will be written by the developer."],
                        ["Future Perfect", "The developer will have written the code.", "The code will have been written by the developer."],
                        ["Modal Verb", "The developer must write the code.", "The code must be written by the developer."],
                        ["Infinitive", "The developer needs to write the code.", "The code needs to be written by the developer."]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "active voice", "passive voice", "subject agent", "action receiver", "be-verb", "past participle"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "The mistake was occurred yesterday.",
                      "correct": "The mistake occurred yesterday.",
                      "why": "'Occur' is an intransitive verb (it doesn't take an object); therefore, it cannot be used in the passive voice."
                    },
                    {
                      "incorrect": "The project completed successfully.",
                      "correct": "The project was completed successfully. (or) The team completed it.",
                      "why": "A project cannot complete itself. The passive 'was completed' is needed to show it received the action."
                    },
                    {
                      "incorrect": "The report is being finished by me yesterday.",
                      "correct": "The report was finished by me yesterday.",
                      "why": "The time marker 'yesterday' requires past passive ('was finished'), not present continuous passive ('is being finished')."
                    },
                    {
                      "incorrect": "The email has been send by the manager.",
                      "correct": "The email has been sent by the manager.",
                      "why": "Always use the V3 past participle ('sent') in passive structures, not the V1 base form ('send')."
                    },
                    {
                      "incorrect": "He was ran over by a car.",
                      "correct": "He was run over by a car.",
                      "why": "The V3 form of 'run' is 'run' (run -> ran -> run). Use V3 'run' in the passive voice."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank with correct passive form: The software _____ (upgrade) by the IT team tonight.",
                      "type": "fill-in-blank",
                      "prefix": "The software ",
                      "suffix": " by the IT team tonight.",
                      "answer": "will be upgraded"
                    },
                    {
                      "question": "Correct the error: 'The email was wrote by the secretary.'",
                      "type": "error-spotting",
                      "sentence": "The email was wrote by the secretary.",
                      "answer": "The email was written by the secretary."
                    },
                    {
                      "question": "Choose the active equivalent of: 'A song is being sung by her.'",
                      "type": "mcq",
                      "options": ["She sings a song.", "She is singing a song.", "She was singing a song.", "She has sung a song."],
                      "answer": "She is singing a song."
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete the passive: 'They built these houses.' -> 'These houses ___ by them.'",
                      "options": ["were built", "was built", "are built", "have built"],
                      "answer": "were built"
                    },
                    {
                      "question": "Choose correct passive form: 'No one has used this laptop.' -> 'This laptop ___.'",
                      "options": ["has not been used", "was not used", "is not being used", "had not been used"],
                      "answer": "has not been used"
                    },
                    {
                      "question": "Make passive: 'Close the door.'",
                      "options": ["Let the door be closed.", "The door will be closed.", "You must close the door."],
                      "answer": "Let the door be closed."
                    },
                    {
                      "question": "Identify which sentence is in the passive voice:",
                      "options": ["The company hired many developers.", "Many developers were hired by the company.", "The company will hire many developers."],
                      "answer": "Many developers were hired by the company."
                    },
                    {
                      "question": "Correct the error: 'The package has sent yesterday.'",
                      "options": ["The package was sent yesterday.", "The package has been sent yesterday.", "The package had sent yesterday."],
                      "answer": "The package was sent yesterday."
                    }
                  ]
                }
                """).build());

        // 15. Direct & Indirect Speech
        lessons.add(Lesson.builder().module("GRAMMAR").section("Direct & Indirect Speech").title("Direct & Indirect Speech").orderIndex(16)
                .content("""
                {
                  "introduction": "Direct and Indirect (Reported) Speech are two different ways of conveying what someone else has said. In **Direct Speech**, you repeat the exact words spoken, enclosed in quotation marks (e.g., He said, 'I am busy.').\\n\\nIn **Indirect Speech**, you report the speaker's words without quotation marks, making necessary adjustments to pronouns, tenses, and time expressions (e.g., 'He said that he was busy.'). This process of shifting tenses backward is called 'backshifting.'\\n\\nIn corporate environments, you constantly report what clients, managers, or colleagues have said during meetings. Getting tenses and pronouns wrong makes reporting confusing and unprofessional. This lesson covers backshifting rules, pronoun and time conversions, and reporting verbs.",
                  "when_to_use": [
                    "Direct: Use when you want to quote someone word-for-word for accuracy or dramatic effect (She remarked, 'This is a masterpiece.')",
                    "Indirect: Use for regular business reporting, summaries, and meeting minutes ('She reported that the project was on track.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Reporting Subject", "type": "primary"},
                      {"label": "Reporting Verb (said/told)", "type": "secondary"},
                      {"label": "Reported Clause (Tense shifted back)", "type": "tertiary"}
                    ],
                    "note": "Use 'say' when there is no direct object; use 'tell' when you mention the listener (e.g., 'said that...' vs 'told me that...')."
                  },
                  "tables": [
                    {
                      "title": "Direct to Indirect Speech Conversions (10 Examples)",
                      "headers": ["Direct Speech", "Indirect Speech Equivalent", "Tense/Word Shift Rule"],
                      "rows": [
                        ["He said, 'I code.'", "He said that he coded.", "Simple Present -> Simple Past"],
                        ["He said, 'I am coding.'", "He said that he was coding.", "Present Continuous -> Past Continuous"],
                        ["He said, 'I have coded.'", "He said that he had coded.", "Present Perfect -> Past Perfect"],
                        ["He said, 'I coded.'", "He said that he had coded.", "Simple Past -> Past Perfect"],
                        ["He said, 'I will code.'", "He said that he would code.", "Will -> Would"],
                        ["He said, 'I can code.'", "He said that he could code.", "Can -> Could"],
                        ["He said, 'I may code.'", "He said that he might code.", "May -> Might"],
                        ["He said, 'I must code.'", "He said that he had to code.", "Must -> Had to"],
                        ["He said, 'I am coding today.'", "He said that he was coding that day.", "today -> that day"],
                        ["He said, 'I coded yesterday.'", "He said that he had coded the day before.", "yesterday -> the day before"]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "direct speech", "indirect speech", "reported speech", "backshifting", "reporting verb", "quotation marks"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "He said me that he was working.",
                      "correct": "He told me that he was working. (or) He said that he was working.",
                      "why": "'Say' cannot take an indirect object directly; use 'tell' when specifying the listener."
                    },
                    {
                      "incorrect": "She said she is working now.",
                      "correct": "She said she was working then.",
                      "why": "Since the reporting verb 'said' is in the past, shift the tenses back ('is' -> 'was') and change 'now' to 'then'."
                    },
                    {
                      "incorrect": "He asked me what was my name.",
                      "correct": "He asked me what my name was.",
                      "why": "In indirect questions, the sentence structure is statement-like (subject + verb), not question-like."
                    },
                    {
                      "incorrect": "The manager said to submit the report.",
                      "correct": "The manager told us to submit the report.",
                      "why": "Use 'told + object + to-infinitive' for reporting commands or requests, rather than 'said to'."
                    },
                    {
                      "incorrect": "She told that she would join the call.",
                      "correct": "She said that she would join the call. (or) She told us that she would join...",
                      "why": "'Told' always requires an object (the listener) in standard reporting."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank with correct indirect speech: 'I am hungry,' he said. -> He said that he _____ hungry.",
                      "type": "fill-in-blank",
                      "prefix": "He said that he ",
                      "suffix": " hungry.",
                      "answer": "was"
                    },
                    {
                      "question": "Correct the error: 'She asked me where did I live.'",
                      "type": "error-spotting",
                      "sentence": "She asked me where did I live.",
                      "answer": "She asked me where I lived."
                    },
                    {
                      "question": "Choose the correct indirect form of: He said, 'I will call you tomorrow.'",
                      "type": "mcq",
                      "options": [
                        "He said that he will call me tomorrow.",
                        "He said that he would call me the next day.",
                        "He said that he would call you tomorrow.",
                        "He told that he would call me tomorrow."
                      ],
                      "answer": "He said that he would call me the next day."
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete the indirect speech: 'I have lost my key.' -> He said that he ___ his key.",
                      "options": ["has lost", "had lost", "lost", "was losing"],
                      "answer": "had lost"
                    },
                    {
                      "question": "Choose correct form: 'Where are you going?' -> He asked me ___.",
                      "options": ["where I was going", "where was I going", "where did I go", "where I am going"],
                      "answer": "where I was going"
                    },
                    {
                      "question": "Report this request: 'Please sit down.' -> He asked me ___.",
                      "options": ["to sit down", "sitting down", "sat down", "please to sit down"],
                      "answer": "to sit down"
                    },
                    {
                      "question": "Identify which is correct indirect speech:",
                      "options": ["She told that she is tired.", "She said she was tired.", "She said to me that she was tired."],
                      "answer": "She said she was tired."
                    },
                    {
                      "question": "Correct the error: 'He told to me that the meeting was postponed.'",
                      "options": ["He told me that the meeting was postponed.", "He said me that the meeting was postponed.", "He asked me that the meeting was postponed."],
                      "answer": "He told me that the meeting was postponed."
                    }
                  ]
                }
                """).build());

        // 16. Conditionals
        lessons.add(Lesson.builder().module("GRAMMAR").section("Advanced Grammar").title("Conditionals").orderIndex(17)
                .content("""
                {
                  "introduction": "Conditional sentences describe situations where one event depends on the occurrence of another. They are often called 'if-clauses' and are highly valuable in professional English for discussing scenarios, possibilities, and regrets.\\n\\nThere are four main types of conditionals: Zero (general truths), First (real future possibilities), Second (imaginary present/future situations), and Third (imaginary past situations/regrets). Each has its own distinct formula combining tenses.\\n\\nMisaligning the tenses in conditional clauses (e.g. saying 'If I will win, I will buy' instead of 'If I win, I will buy') is a highly visible grammatical error. This lesson details each type, its formula, and usage rules.",
                  "when_to_use": [
                    "Zero Conditional: general truths, facts, or physical rules ('If you heat ice, it melts.')",
                    "First Conditional: real, likely future outcomes ('If it rains, we will stay home.')",
                    "Second Conditional: hypothetical or imaginary present/future states ('If I had a million dollars, I would travel.')",
                    "Third Conditional: past regrets or imaginary past events ('If I had studied, I would have passed.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "If Clause (Condition)", "type": "primary"},
                      {"label": "Comma (,)", "type": "secondary"},
                      {"label": "Main Clause (Result)", "type": "tertiary"}
                    ],
                    "note": "If the 'if' clause comes first, use a comma; if the result clause comes first, do not use a comma."
                  },
                  "tables": [
                    {
                      "title": "The Four Conditional Structures (10 Examples)",
                      "headers": ["Conditional Type", "If Clause Tense", "Result Clause Tense", "Example Sentence"],
                      "rows": [
                        ["Zero", "Simple Present", "Simple Present", "If you boil water, it turns to steam."],
                        ["Zero", "Simple Present", "Simple Present", "If the server goes down, the alarm rings."],
                        ["First", "Simple Present", "will + V1", "If they hire freshers, I will apply."],
                        ["First", "Simple Present", "will + V1", "If he emails me, I will reply immediately."],
                        ["Second", "Simple Past", "would + V1", "If I were the CEO, I would increase salaries."],
                        ["Second", "Simple Past", "would + V1", "If we had more budget, we would buy it."],
                        ["Third", "Past Perfect", "would have + V3", "If we had tested it, we would have avoided the bug."],
                        ["Third", "Past Perfect", "would have + V3", "If she had arrived early, she would have met him."],
                        ["Mixed", "Past Perfect", "would + V1", "If I had taken the job, I would be rich now."],
                        ["Mixed", "Simple Past", "would have + V3", "If I were smart, I would have studied."]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "zero conditional", "first conditional", "second conditional", "third conditional", "hypothetical", "subjunctive"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "If I will reach early, I will call you.",
                      "correct": "If I reach early, I will call you.",
                      "why": "Do not use 'will' in the 'if' clause of the First Conditional; use Simple Present instead."
                    },
                    {
                      "incorrect": "If I was the President, I would change the law.",
                      "correct": "If I were the President, I would change the law.",
                      "why": "In imaginary situations (Second Conditional), the subjunctive form 'were' is used for all subjects (including 'I' and 'he/she/it')."
                    },
                    {
                      "incorrect": "If I would have known, I would have told you.",
                      "correct": "If I had known, I would have told you.",
                      "why": "Do not use 'would have' in the 'if' clause of the Third Conditional; use Past Perfect ('had known')."
                    },
                    {
                      "incorrect": "If it rains, we stay home.",
                      "correct": "If it rains, we will stay home. (unless stating a general habit)",
                      "why": "Specific future events require the First Conditional (will + V1) in the result clause."
                    },
                    {
                      "incorrect": "If we had budget, we will buy it.",
                      "correct": "If we had budget, we would buy it.",
                      "why": "Match the tenses correctly: Simple Past ('had') in the 'if' clause requires 'would' (not 'will') in the result clause."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank with correct tense: If you _____ (study) hard, you will pass the placement test.",
                      "type": "fill-in-blank",
                      "prefix": "If you ",
                      "suffix": " hard, you will pass the placement test.",
                      "answer": "study"
                    },
                    {
                      "question": "Correct the error: 'If I had been there, I would help you.'",
                      "type": "error-spotting",
                      "sentence": "If I had been there, I would help you.",
                      "answer": "If I had been there, I would have helped you."
                    },
                    {
                      "question": "Choose the correct verb: 'If I _____ you, I would accept the job offer.'",
                      "type": "mcq",
                      "options": ["am", "was", "were", "be"],
                      "answer": "were"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'If you heat ice, it ___.'",
                      "options": ["melt", "melts", "will melt", "melted"],
                      "answer": "melts"
                    },
                    {
                      "question": "Choose correct: 'If they ___ early, they would have caught the bus.'",
                      "options": ["leave", "left", "had left", "has left"],
                      "answer": "had left"
                    },
                    {
                      "question": "Make negative result: 'If it rains, ___.'",
                      "options": ["we will not go.", "we wouldn't go.", "we had not gone."],
                      "answer": "we will not go."
                    },
                    {
                      "question": "Make question: 'What ___ you do if you won the lottery?'",
                      "options": ["will", "would", "do", "did"],
                      "answer": "would"
                    },
                    {
                      "question": "Correct the error: 'If she has time, she would join us.'",
                      "options": ["If she had time, she would join us.", "If she has time, she will join us.", "Both options are correct."],
                      "answer": "Both options are correct."
                    }
                  ]
                }
                """).build());

        // 17. Modals
        lessons.add(Lesson.builder().module("GRAMMAR").section("Advanced Grammar").title("Modals").orderIndex(18)
                .content("""
                {
                  "introduction": "Modals (modal auxiliary verbs) are helping verbs that express ability, permission, advice, necessity, obligation, or possibility. They include can, could, may, might, must, shall, should, will, and would.\\n\\nModals are unique: they never change form (no adding '-s' or '-ed'), and they are always followed by the base form (V1) of the main verb without 'to' (e.g. 'I should study', not 'I should to study').\\n\\nIn professional environments, using the correct modal is the key to politeness, soft skills, and directness. For example, saying 'You must do this' sounds aggressive, whereas 'You should do this' or 'Could you do this?' sounds polite and collaborative. This lesson clarifies modal usages and common pitfalls.",
                  "when_to_use": [
                    "Ability: can (present), could (past) ('I can code in Java.')",
                    "Permission: can (informal), may (formal) ('May I ask a question?')",
                    "Advice/Recommendation: should, ought to ('You should review the documentation.')",
                    "Obligation/Necessity: must, have to ('We must submit the audit by Friday.')",
                    "Possibility: may, might, could ('It might rain today.')"
                  ],
                  "formula": {
                    "parts": [
                      {"label": "Subject", "type": "primary"},
                      {"label": "Modal Verb", "type": "secondary"},
                      {"label": "Verb (base V1 form)", "type": "tertiary"}
                    ],
                    "note": "Modals do not take suffixes (no 'cans', 'shoulded') and never take the infinitive 'to' after them."
                  },
                  "tables": [
                    {
                      "title": "Modal Meanings and Examples (10 Examples)",
                      "headers": ["Modal Verb", "Core Meaning", "Sentence Example"],
                      "rows": [
                        ["can", "Ability", "She can speak German fluently."],
                        ["could", "Polite Request", "Could you review my code, please?"],
                        ["may", "Formal Permission", "May I leave the meeting early?"],
                        ["might", "Weak Possibility", "We might launch the product next week."],
                        ["must", "Strong Obligation", "You must wear a helmet while riding."],
                        ["have to", "External Necessity", "I have to log in by 9:00 AM daily."],
                        ["should", "Advice / Recommendation", "You should prepare for the interview."],
                        ["would", "Hypothetical Outcome", "I would help you if I had time."],
                        ["shall", "Formal Suggestion", "Shall we begin the presentation?"],
                        ["ought to", "Moral Obligation", "We ought to respect our colleagues."]
                      ]
                    }
                  ],
                  "time_expressions": [
                    "modal auxiliary", "ability", "permission", "obligation", "polite request", "possibility"
                  ],
                  "mistakes": [
                    {
                      "incorrect": "I can to play the piano.",
                      "correct": "I can play the piano.",
                      "why": "Do not use 'to' after modal auxiliary verbs like 'can', 'should', 'must', 'will'."
                    },
                    {
                      "incorrect": "He musts attend the meeting.",
                      "correct": "He must attend the meeting.",
                      "why": "Modal verbs do not take an '-s' or '-es' for third-person singular subjects."
                    },
                    {
                      "incorrect": "We shoulded completed the project yesterday.",
                      "correct": "We should have completed the project yesterday.",
                      "why": "Modals cannot be inflected for past tense. To express past recommendation or regret, use 'should have + V3'."
                    },
                    {
                      "incorrect": "I might to join the call.",
                      "correct": "I might join the call.",
                      "why": "Do not place the infinitive 'to' after the modal verb 'might'."
                    },
                    {
                      "incorrect": "You don't must smoke here.",
                      "correct": "You must not smoke here. (or) You don't have to smoke here.",
                      "why": "'Must not' denotes prohibition. 'Don't have to' denotes lack of obligation."
                    }
                  ],
                  "exercises": [
                    {
                      "question": "Fill in the blank with correct modal: You _____ study hard to clear the placement test. (advice)",
                      "type": "fill-in-blank",
                      "prefix": "You ",
                      "suffix": " study hard to clear the placement test.",
                      "answer": "should"
                    },
                    {
                      "question": "Correct the error: 'Could you please to help me?'",
                      "type": "error-spotting",
                      "sentence": "Could you please to help me?",
                      "answer": "Could you please help me?"
                    },
                    {
                      "question": "Choose the correct modal for formal permission: '_____ I use your phone, sir?'",
                      "type": "mcq",
                      "options": ["Can", "May", "Should", "Must"],
                      "answer": "May"
                    }
                  ],
                  "quiz": [
                    {
                      "question": "Complete: 'We ___ respect our elders.'",
                      "options": ["should", "must", "might", "can"],
                      "answer": "should"
                    },
                    {
                      "question": "Choose correct: 'I ___ play tennis when I was young.'",
                      "options": ["can", "could", "should", "must"],
                      "answer": "could"
                    },
                    {
                      "question": "Make negative advice: 'You should go.'",
                      "options": ["You should not go.", "You don't should go.", "You shouldn't to go."],
                      "answer": "You should not go."
                    },
                    {
                      "question": "Make request: '___ you open the window?'",
                      "options": ["Should", "Could", "May", "Might"],
                      "answer": "Could"
                    },
                    {
                      "question": "Correct the error: 'I must have left my keys at the office.'",
                      "options": ["I must left my keys.", "I must have left my keys. (No error)", "I must to leave my keys."],
                      "answer": "I must have left my keys. (No error)"
                    }
                  ]
                }
                """).build());
    }

    private void seedVocabularyLessons(java.util.Set<String> existingKeys) {
        List<Lesson> lessons = new ArrayList<>();

        String getActiveContent = """
            {
              "title": "Phrasal Verbs with GET",
              "level": "Intermediate",
              "duration": "15 min",
              "xp": "+50 XP",
              "intro": "The verb 'get' is one of the most versatile in English. When combined with particles, it forms dozens of phrasal verbs. Here are the most essential ones you need to master.",
              "words": [
                {
                  "word": "get up",
                  "pos": "verb",
                  "ipa": "/ɡet ʌp/",
                  "definition": "To rise from bed after sleeping; to stand up.",
                  "meanings": [
                    {"num": 1, "context": "Daily routine", "sentence": "I usually get up at 6 AM every morning."}
                  ],
                  "memory_tip": "Think of moving 'up'wards away from your flat bed.",
                  "synonyms": ["rise", "stand"],
                  "antonyms": ["lie down", "sleep"]
                },
                {
                  "word": "get on",
                  "pos": "verb",
                  "ipa": "/ɡet ɒn/",
                  "definition": "To board a bus, train, or plane; to have a friendly relationship.",
                  "meanings": [
                    {"num": 1, "context": "Transit", "sentence": "We need to get on the bus before it leaves."},
                    {"num": 2, "context": "Social", "sentence": "Do you get on well with your colleagues?"}
                  ],
                  "memory_tip": "Imagine stepping 'on' a platform or board.",
                  "synonyms": ["board", "agree"],
                  "antonyms": ["get off", "argue"]
                },
                {
                  "word": "get off",
                  "pos": "verb",
                  "ipa": "/ɡet ɒf/",
                  "definition": "To disembark from a vehicle; to escape punishment or duty.",
                  "meanings": [
                    {"num": 1, "context": "Transit", "sentence": "I get off at the next subway station."},
                    {"num": 2, "context": "Legal", "sentence": "He got off with a small warning fine."}
                  ],
                  "memory_tip": "Stepping 'off' the vehicle floor onto solid ground.",
                  "synonyms": ["disembark", "escape"],
                  "antonyms": ["get on", "enter"]
                },
                {
                  "word": "get over",
                  "pos": "verb",
                  "ipa": "/ɡet ˈəʊvə/",
                  "definition": "To recover from an illness, loss, or difficulty; to overcome a mental or emotional challenge.",
                  "meanings": [
                    {"num": 1, "context": "Illness", "sentence": "It took her a week to get over the flu."},
                    {"num": 2, "context": "Emotional", "sentence": "You will get over your fear of public speaking with practice."}
                  ],
                  "memory_tip": "Climbing 'over' a tall fence of problems.",
                  "synonyms": ["recover", "overcome"],
                  "antonyms": ["succumb", "suffer"]
                },
                {
                  "word": "get through",
                  "pos": "verb",
                  "ipa": "/ɡet θruː/",
                  "definition": "To finish a task; to survive a difficult period; to make contact by phone.",
                  "meanings": [
                    {"num": 1, "context": "Completion", "sentence": "I have a lot of work to get through today."},
                    {"num": 2, "context": "Contact", "sentence": "I tried to call her, but I couldn't get through."}
                  ],
                  "memory_tip": "Passing 'through' a thick forest to reach the end.",
                  "synonyms": ["finish", "connect"],
                  "antonyms": ["fail", "give up"]
                },
                {
                  "word": "get along",
                  "pos": "verb",
                  "ipa": "/ɡet əˈlɒŋ/",
                  "definition": "To have a harmonious relationship with someone.",
                  "meanings": [
                    {"num": 1, "context": "Relationship", "sentence": "They get along like house on fire."}
                  ],
                  "memory_tip": "Walking 'along' the same path together with a friend.",
                  "synonyms": ["harmonize", "befriend"],
                  "antonyms": ["clash", "fight"]
                },
                {
                  "word": "get away",
                  "pos": "verb",
                  "ipa": "/ɡet əˈweɪ/",
                  "definition": "To escape or leave, especially for a holiday or from a tight spot.",
                  "meanings": [
                    {"num": 1, "context": "Escape", "sentence": "The thief managed to get away from the police."},
                    {"num": 2, "context": "Vacation", "sentence": "We hope to get away for a few days next month."}
                  ],
                  "memory_tip": "Going 'away' from your usual routine or danger.",
                  "synonyms": ["escape", "flee"],
                  "antonyms": ["stay", "return"]
                },
                {
                  "word": "get back",
                  "pos": "verb",
                  "ipa": "/ɡet bæk/",
                  "definition": "To return to a place or state; to retrieve something.",
                  "meanings": [
                    {"num": 1, "context": "Return", "sentence": "When did you get back from Chennai?"}
                  ],
                  "memory_tip": "Moving 'back'wards to your origin.",
                  "synonyms": ["return", "recover"],
                  "antonyms": ["leave", "depart"]
                },
                {
                  "word": "get in",
                  "pos": "verb",
                  "ipa": "/ɡet ɪn/",
                  "definition": "To enter a car, taxi, or building; to arrive at a destination.",
                  "meanings": [
                    {"num": 1, "context": "Entry", "sentence": "Get in the car, it's starting to pour!"}
                  ],
                  "memory_tip": "Moving 'in'side a small container or space.",
                  "synonyms": ["enter", "arrive"],
                  "antonyms": ["get out", "exit"]
                },
                {
                  "word": "get out",
                  "pos": "verb",
                  "ipa": "/ɡet aʊt/",
                  "definition": "To exit a vehicle or room; to become known (secrets).",
                  "meanings": [
                    {"num": 1, "context": "Exit", "sentence": "He got out of the taxi and paid the fare."}
                  ],
                  "memory_tip": "Moving 'out'wards to open space.",
                  "synonyms": ["exit", "escape"],
                  "antonyms": ["get in", "enter"]
                },
                {
                  "word": "get to",
                  "pos": "verb",
                  "ipa": "/ɡet tuː/",
                  "definition": "To reach a destination; to annoy someone; to start doing something.",
                  "meanings": [
                    {"num": 1, "context": "Destination", "sentence": "We got to the station just in time."},
                    {"num": 2, "context": "Irritation", "sentence": "Don't let his comments get to you."}
                  ],
                  "memory_tip": "Moving 'to'wards a final point.",
                  "synonyms": ["reach", "bother"],
                  "antonyms": ["leave", "avoid"]
                },
                {
                  "word": "get together",
                  "pos": "verb",
                  "ipa": "/ɡet təˈɡeð.ər/",
                  "definition": "To meet socially or to collaborate.",
                  "meanings": [
                    {"num": 1, "context": "Socializing", "sentence": "Let's get together for coffee next weekend."}
                  ],
                  "memory_tip": "Gathering 'together' in a group.",
                  "synonyms": ["meet", "assemble"],
                  "antonyms": ["disperse", "separate"]
                }
              ],
              "practice": {
                "question": "Which phrasal verb means 'to have a good relationship'?",
                "options": ["get off", "get along", "get away", "get up"],
                "answer": "get along"
              }
            }
            """;

        String genericVocabContent = """
            {
              "title": "Vocabulary Expansion",
              "level": "Intermediate",
              "duration": "10 min",
              "xp": "+30 XP",
              "word": "Analyze",
              "pos": "verb",
              "ipa": "/ˈæn.əl.aɪz/",
              "definition": "To examine methodically and in detail the constitution or structure of something.",
              "meanings": [
                {"num": 1, "context": "Detailed analysis", "sentence": "We need to analyze the project results."}
              ],
              "insight": "Analyzing helps break complex topics into manageable pieces.",
              "memory_tip": "Think of dissecting something to inspect its components.",
              "word_family": [
                {"type": "Noun", "value": "Analysis"},
                {"type": "Verb", "value": "Analyze"},
                {"type": "Adjective", "value": "Analytical"}
              ],
              "usage_table": {
                "title": "Usage in Context",
                "headers": ["Scenario", "Correct Sentence", "Explanation"],
                "rows": [
                  ["Business", "The consultant will analyze our financials.", "Evaluating performance."]
                ]
              },
              "synonyms": ["Examine", "Inspect", "Scrutinize"],
              "antonyms": ["Synthesize", "Ignore"],
              "practice": {
                "question": "Complete the sentence using the correct form of 'Analyze':",
                "prompt": "The system will [input] the incoming data automatically.",
                "answer": "analyze"
              }
            }
            """;


        // All registrations are consolidated at the bottom.

        // Phrasal Verbs – content strings defined below; all registrations at bottom

        String makePhrasalContent = """
            {
              "title": "Phrasal Verbs with MAKE",
              "level": "Intermediate",
              "duration": "12 min",
              "xp": "+50 XP",
              "intro": "### Phrasal Verbs with MAKE\\n\\nThe verb **make** is one of the most powerful and frequently used verbs in English. When combined with a particle (up, out, for, off, over, do, it, sure, way, up for, sense of), it creates a completely new meaning that is often impossible to guess from the individual words.\\n\\nMastering MAKE phrasal verbs will dramatically improve your spoken fluency, help you understand movies and conversations, and make you sound far more natural in interviews and group discussions. This lesson covers all 12 essential MAKE phrasal verbs used in everyday and professional English.",
              "words": [
                {
                  "word": "make up",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ʌp/",
                  "definition": "To reconcile after a disagreement; to invent a story or excuse; to apply cosmetics.",
                  "meanings": [
                    {"num": 1, "context": "Reconciliation", "sentence": "After the argument, they decided to make up and move forward as a team."},
                    {"num": 2, "context": "Invention", "sentence": "Don't make up excuses — just tell the manager the truth about the delay."}
                  ],
                  "memory_tip": "Imagine 'building up' the relationship from scratch — or painting your face 'up' with makeup.",
                  "synonyms": ["reconcile", "invent", "fabricate"],
                  "antonyms": ["fall out", "reveal", "tell the truth"]
                },
                {
                  "word": "make out",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk aʊt/",
                  "definition": "To understand or decipher something with difficulty; to manage or cope; to claim or suggest.",
                  "meanings": [
                    {"num": 1, "context": "Understanding", "sentence": "I couldn't make out what the client was saying — the call kept breaking."},
                    {"num": 2, "context": "Claiming", "sentence": "He made out that he had already submitted the report, but he hadn't."}
                  ],
                  "memory_tip": "Think of trying to 'pull out' meaning from a blurry, unclear situation.",
                  "synonyms": ["decipher", "understand", "claim"],
                  "antonyms": ["misunderstand", "confuse"]
                },
                {
                  "word": "make for",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk fɔː/",
                  "definition": "To move towards a place; to contribute to a particular result or condition.",
                  "meanings": [
                    {"num": 1, "context": "Direction", "sentence": "After the meeting, everyone made for the exit quickly."},
                    {"num": 2, "context": "Contribution", "sentence": "A clear agenda makes for a much more productive team discussion."}
                  ],
                  "memory_tip": "You are 'headed for' something — going toward a destination or outcome.",
                  "synonyms": ["head towards", "contribute to", "promote"],
                  "antonyms": ["avoid", "hinder"]
                },
                {
                  "word": "make off",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ɒf/",
                  "definition": "To escape or leave hurriedly, often after doing something wrong.",
                  "meanings": [
                    {"num": 1, "context": "Escape", "sentence": "The thief made off with the laptop before anyone could react."},
                    {"num": 2, "context": "Informal", "sentence": "Once the presentation ended, the interns made off before anyone assigned more work."}
                  ],
                  "memory_tip": "The criminal 'makes themselves off' the scene — disappears quickly.",
                  "synonyms": ["flee", "escape", "bolt"],
                  "antonyms": ["stay", "remain", "arrive"]
                },
                {
                  "word": "make over",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ˈəʊvə/",
                  "definition": "To completely change the appearance or function of someone or something; to transfer ownership legally.",
                  "meanings": [
                    {"num": 1, "context": "Transformation", "sentence": "They made over the old conference room into a modern collaborative workspace."},
                    {"num": 2, "context": "Legal transfer", "sentence": "He made over the property to his son before leaving the country."}
                  ],
                  "memory_tip": "Think of a TV makeover show — everything changes completely.",
                  "synonyms": ["transform", "renovate", "transfer"],
                  "antonyms": ["preserve", "maintain", "keep"]
                },
                {
                  "word": "make do",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk duː/",
                  "definition": "To manage with whatever is available, even if it is not ideal.",
                  "meanings": [
                    {"num": 1, "context": "Limited resources", "sentence": "We don't have the ideal tools, so we'll have to make do with what we have."},
                    {"num": 2, "context": "Everyday use", "sentence": "During the power cut, they made do with candles and phone flashlights."}
                  ],
                  "memory_tip": "You 'do' what you can 'make' work — improvise with limited options.",
                  "synonyms": ["manage", "cope", "improvise"],
                  "antonyms": ["demand more", "refuse", "give up"]
                },
                {
                  "word": "make it",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ɪt/",
                  "definition": "To succeed in reaching a place or achieving a goal; to survive a difficult situation.",
                  "meanings": [
                    {"num": 1, "context": "Success", "sentence": "Despite the competition, she made it to the final round of interviews."},
                    {"num": 2, "context": "Arrival", "sentence": "I'm stuck in traffic — I don't think I'll make it to the meeting on time."}
                  ],
                  "memory_tip": "Making it to the finish line — getting there, beating the challenge.",
                  "synonyms": ["succeed", "arrive", "survive"],
                  "antonyms": ["fail", "miss", "give up"]
                },
                {
                  "word": "make sure",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ʃʊə/",
                  "definition": "To check or ensure that something is done correctly or a condition is met.",
                  "meanings": [
                    {"num": 1, "context": "Checking", "sentence": "Make sure you save the document before closing the application."},
                    {"num": 2, "context": "Professional", "sentence": "The manager asked us to make sure all deliverables were reviewed before submission."}
                  ],
                  "memory_tip": "You 'make' something 'sure' — you confirm it is definitely true or done.",
                  "synonyms": ["ensure", "verify", "confirm"],
                  "antonyms": ["ignore", "overlook", "assume"]
                },
                {
                  "word": "make way",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk weɪ/",
                  "definition": "To create space or a path for someone or something to pass; to step aside for something new.",
                  "meanings": [
                    {"num": 1, "context": "Physical", "sentence": "Make way for the ambulance — it needs to pass through immediately."},
                    {"num": 2, "context": "Figurative", "sentence": "Older technology must make way for newer, more efficient solutions."}
                  ],
                  "memory_tip": "You 'make a way' — clear the path so something can move forward.",
                  "synonyms": ["clear the path", "step aside", "yield"],
                  "antonyms": ["block", "obstruct", "impede"]
                },
                {
                  "word": "make up for",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ʌp fɔː/",
                  "definition": "To compensate for a loss, mistake, or deficiency.",
                  "meanings": [
                    {"num": 1, "context": "Compensation", "sentence": "Working overtime this weekend will make up for the lost hours during the holiday."},
                    {"num": 2, "context": "Personal", "sentence": "He brought flowers to make up for forgetting their anniversary."}
                  ],
                  "memory_tip": "You 'build up' what was missing — filling in the gap to restore balance.",
                  "synonyms": ["compensate", "offset", "atone for"],
                  "antonyms": ["worsen", "add to the problem"]
                },
                {
                  "word": "make sense of",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk sɛns ɒv/",
                  "definition": "To understand something that is confusing or complex.",
                  "meanings": [
                    {"num": 1, "context": "Data analysis", "sentence": "It took the team hours to make sense of the inconsistent test results."},
                    {"num": 2, "context": "Learning", "sentence": "Watch the video again — it will help you make sense of the grammar rule."}
                  ],
                  "memory_tip": "You 'manufacture meaning' from something chaotic or unclear.",
                  "synonyms": ["understand", "decipher", "interpret"],
                  "antonyms": ["confuse", "misinterpret", "misread"]
                },
                {
                  "word": "make a point of",
                  "pos": "verb (phrasal)",
                  "ipa": "/meɪk ə pɔɪnt ɒv/",
                  "definition": "To do something deliberately and consciously; to treat something as a priority.",
                  "meanings": [
                    {"num": 1, "context": "Professional habit", "sentence": "She makes a point of arriving 15 minutes early to every client meeting."},
                    {"num": 2, "context": "Communication", "sentence": "I made a point of congratulating each team member after the product launch."}
                  ],
                  "memory_tip": "You 'put a dot on it' — you make that action intentional and deliberate.",
                  "synonyms": ["prioritize", "ensure deliberately", "emphasize"],
                  "antonyms": ["neglect", "overlook", "forget"]
                }
              ],
              "mistakes": [
                {
                  "incorrect": "I made up a reason because I was late.",
                  "correct": "I made up an excuse because I was late.",
                  "why": "'Reason' is a neutral or logical explanation; 'excuse' is what you invent to justify yourself."
                },
                {
                  "incorrect": "We made off the room for the new team.",
                  "correct": "We made way for the new team. / We cleared the room for the new team.",
                  "why": "'Make off' means to flee/escape — not to clear space. Use 'make way' for creating room."
                },
                {
                  "incorrect": "She couldn't make out the problem, so she gave up.",
                  "correct": "She couldn't make sense of the problem, so she gave up.",
                  "why": "'Make out' means to barely perceive/hear. 'Make sense of' means to understand something complex."
                },
                {
                  "incorrect": "I will make up the missed hours by working extra.",
                  "correct": "I will make up for the missed hours by working extra.",
                  "why": "'Make up for' requires the preposition 'for' when referring to compensation."
                },
                {
                  "incorrect": "Make sure to check the report before submit.",
                  "correct": "Make sure to check the report before submitting.",
                  "why": "After prepositions like 'before', use the gerund (-ing form), not the base verb."
                }
              ],
              "real_world_scenarios": [
                {
                  "context": "Job Interview Answer",
                  "content": "Interviewer: 'Describe a time you handled limited resources.'\\nCandidate: 'During our product launch, our main server went down. We had to make do with a backup server and a reduced team. I made sure every critical function was still running and made up for the lost time by working through the weekend. We made it to the launch date successfully.'"
                },
                {
                  "context": "Professional Email",
                  "content": "Subject: Compensation for Delayed Delivery\\nDear Mr. Sharma,\\nWe sincerely apologize for the delay in the project. We will make up for the lost time by assigning an additional resource to your account. We will make sure the final deliverable reaches you by Friday without fail."
                },
                {
                  "context": "Casual Conversation",
                  "content": "A: 'I heard you and Priya had a falling out last week.'\\nB: 'Yes, but we made up yesterday. It was just a misunderstanding. I couldn't make out why she was upset at first, but once she explained, it all made sense.'"
                }
              ],
              "exercises": [
                {
                  "question": "Fill in the blank: 'She _____ for being late by staying an extra hour at the office.'",
                  "type": "fill-in-blank",
                  "prefix": "She",
                  "suffix": "for being late by staying an extra hour at the office.",
                  "answer": "made up"
                },
                {
                  "question": "Which phrasal verb means 'to manage with limited resources'?",
                  "type": "mcq",
                  "options": ["make out", "make do", "make off", "make over"],
                  "answer": "make do"
                },
                {
                  "question": "Spot the error: 'The thief made out with the cash before the police arrived.'",
                  "type": "error-spotting",
                  "sentence": "The thief made out with the cash before the police arrived.",
                  "answer": "The thief made off with the cash before the police arrived."
                }
              ],
              "quiz": [
                {
                  "question": "What does 'make up' mean in the sentence: 'Let's make up after our argument'?",
                  "options": ["Apply cosmetics", "Reconcile and forgive", "Invent a story", "Compensate"],
                  "answer": "Reconcile and forgive"
                },
                {
                  "question": "Which phrasal verb means 'to escape quickly'?",
                  "options": ["make for", "make do", "make off", "make it"],
                  "answer": "make off"
                },
                {
                  "question": "Fill: 'I couldn't _____ what he was saying — the audio was too unclear.'",
                  "options": ["make out", "make up", "make sure", "make way"],
                  "answer": "make out"
                },
                {
                  "question": "Which phrasal verb is correct: 'We need to _____ the budget shortfall by cutting non-essential expenses.'",
                  "options": ["make up for", "make do for", "make out for", "make off for"],
                  "answer": "make up for"
                },
                {
                  "question": "True or False: 'Make a point of' means to do something accidentally.",
                  "options": ["True", "False"],
                  "answer": "False"
                }
              ],
              "faqs": [
                {
                  "question": "Are MAKE phrasal verbs mostly formal or informal?",
                  "answer": "Most are informal or semi-formal. However, 'make sure', 'make a point of', and 'make way for' are commonly used in professional emails and business communication."
                },
                {
                  "question": "What is the difference between 'make up' and 'make up for'?",
                  "answer": "'Make up' means to reconcile or invent. 'Make up for' means to compensate — it always requires the preposition 'for' and a specific deficiency: 'make up for the delay', 'make up for the loss'."
                },
                {
                  "question": "Can 'make out' mean the same as 'understand'?",
                  "answer": "Yes, but it specifically means to barely understand something that is unclear or hard to perceive — like a muffled voice or blurry writing. For complete understanding, use 'understand' or 'make sense of'."
                },
                {
                  "question": "How is 'make for' used in modern English?",
                  "answer": "It has two uses: (1) Moving toward a place — 'We made for the exit'; (2) Contributing to a result — 'Good planning makes for a smooth project'. The second usage is more formal and common in writing."
                },
                {
                  "question": "Is 'make it' always about success?",
                  "answer": "Not always. It can mean arriving on time ('I'll make it to the meeting') or surviving a difficult situation ('The patient is critical, but the doctors think he'll make it'). Context determines the meaning."
                }
              ],
              "related_topics": [
                {"title": "Phrasal Verbs with GET"},
                {"title": "Phrasal Verbs with TAKE"},
                {"title": "Phrasal Verbs with DO"},
                {"title": "Confusing Words: Make vs Do"}
              ]
            }
            """;

        // ── BATCH 1 content strings: TAKE, COME, GO ──────────────────────────
        String takePhrasalContent = """
            {
              "title": "Phrasal Verbs with TAKE",
              "level": "Intermediate",
              "duration": "15 min",
              "xp": "+50 XP",
              "intro": "**TAKE** is one of the most productive verbs in English. Combined with particles it covers everything from accepting responsibility to removing things. Mastering these phrasal verbs will make your spoken and written English sound natural and confident.",
              "words": [
                {
                  "word": "take up",
                  "pos": "verb",
                  "ipa": "/teɪk ʌp/",
                  "definition": "To begin a new hobby, activity, or habit; to occupy space or time.",
                  "meanings": [
                    {"num": 1, "context": "New hobby", "sentence": "She decided to take up yoga after her doctor's advice."},
                    {"num": 2, "context": "Space/time", "sentence": "This project is taking up all of my weekends."},
                    {"num": 3, "context": "Offer", "sentence": "I will take up your offer of help gratefully."}
                  ],
                  "memory_tip": "Think of picking something 'up' and starting to carry it with you — you've taken it up as your own.",
                  "synonyms": ["start", "begin", "adopt"],
                  "antonyms": ["drop", "abandon", "quit"]
                },
                {
                  "word": "take on",
                  "pos": "verb",
                  "ipa": "/teɪk ɒn/",
                  "definition": "To accept a challenge, responsibility, or employee; to hire someone.",
                  "meanings": [
                    {"num": 1, "context": "Responsibility", "sentence": "He agreed to take on the role of team leader."},
                    {"num": 2, "context": "Hiring", "sentence": "The company took on twenty new graduates this year."},
                    {"num": 3, "context": "Competition", "sentence": "They plan to take on their biggest rival in the final."}
                  ],
                  "memory_tip": "You put something 'on' your shoulders — accepting the weight of responsibility.",
                  "synonyms": ["accept", "hire", "undertake"],
                  "antonyms": ["refuse", "dismiss", "reject"]
                },
                {
                  "word": "take off",
                  "pos": "verb",
                  "ipa": "/teɪk ɒf/",
                  "definition": "To leave the ground (plane); to remove clothing; to become suddenly successful.",
                  "meanings": [
                    {"num": 1, "context": "Aviation", "sentence": "The plane will take off in twenty minutes."},
                    {"num": 2, "context": "Clothing", "sentence": "Please take off your shoes before entering."},
                    {"num": 3, "context": "Success", "sentence": "Her career really took off after that TED talk."}
                  ],
                  "memory_tip": "Something moves 'off' the ground or away — it's launching or departing.",
                  "synonyms": ["depart", "remove", "launch"],
                  "antonyms": ["land", "put on", "stagnate"]
                },
                {
                  "word": "take over",
                  "pos": "verb",
                  "ipa": "/teɪk ˈəʊvə/",
                  "definition": "To assume control or ownership of something from someone else.",
                  "meanings": [
                    {"num": 1, "context": "Business", "sentence": "A larger firm took over the startup last quarter."},
                    {"num": 2, "context": "Role", "sentence": "Can you take over my shift tonight? I'm unwell."}
                  ],
                  "memory_tip": "Imagine stepping 'over' the previous owner and sitting in their seat.",
                  "synonyms": ["acquire", "assume control", "seize"],
                  "antonyms": ["surrender", "relinquish", "hand over"]
                },
                {
                  "word": "take back",
                  "pos": "verb",
                  "ipa": "/teɪk bæk/",
                  "definition": "To retract a statement; to return a purchased item; to reclaim something.",
                  "meanings": [
                    {"num": 1, "context": "Retract", "sentence": "I take back everything I said — you were right."},
                    {"num": 2, "context": "Return", "sentence": "She took the dress back to the shop for a refund."}
                  ],
                  "memory_tip": "Moving something 'back' to where it originally was — physically or verbally.",
                  "synonyms": ["retract", "reclaim", "return"],
                  "antonyms": ["keep", "retain", "stand by"]
                },
                {
                  "word": "take out",
                  "pos": "verb",
                  "ipa": "/teɪk aʊt/",
                  "definition": "To remove from a place; to invite someone on a social outing; to obtain (loan/insurance).",
                  "meanings": [
                    {"num": 1, "context": "Remove", "sentence": "Please take out the garbage before you leave."},
                    {"num": 2, "context": "Social", "sentence": "He took her out to a lovely restaurant for dinner."},
                    {"num": 3, "context": "Finance", "sentence": "They took out a mortgage to buy the apartment."}
                  ],
                  "memory_tip": "You move something 'out' from inside — extracting or escorting outward.",
                  "synonyms": ["remove", "extract", "escort"],
                  "antonyms": ["put in", "insert", "stay in"]
                },
                {
                  "word": "take in",
                  "pos": "verb",
                  "ipa": "/teɪk ɪn/",
                  "definition": "To absorb information; to accommodate someone; to deceive or trick.",
                  "meanings": [
                    {"num": 1, "context": "Absorb", "sentence": "I couldn't take in everything the professor said."},
                    {"num": 2, "context": "Accommodate", "sentence": "They kindly took in the stray cat from the street."},
                    {"num": 3, "context": "Deceived", "sentence": "I was completely taken in by his convincing story."}
                  ],
                  "memory_tip": "Bringing something 'in' to your mind or home — absorbing it fully.",
                  "synonyms": ["absorb", "shelter", "deceive"],
                  "antonyms": ["reject", "expel", "see through"]
                },
                {
                  "word": "take away",
                  "pos": "verb",
                  "ipa": "/teɪk əˈweɪ/",
                  "definition": "To remove something; the key lesson or point learned from an experience.",
                  "meanings": [
                    {"num": 1, "context": "Remove", "sentence": "Nothing can take away the joy of that moment."},
                    {"num": 2, "context": "Lesson", "sentence": "The main takeaway from the meeting was to act fast."}
                  ],
                  "memory_tip": "Carrying something 'away' — reducing what was there or extracting the essence.",
                  "synonyms": ["remove", "subtract", "diminish"],
                  "antonyms": ["add", "restore", "return"]
                },
                {
                  "word": "take apart",
                  "pos": "verb",
                  "ipa": "/teɪk əˈpɑːt/",
                  "definition": "To disassemble something into pieces; to criticize something severely.",
                  "meanings": [
                    {"num": 1, "context": "Disassemble", "sentence": "He took apart the old radio to fix the wiring."},
                    {"num": 2, "context": "Criticism", "sentence": "The critics took her debut novel apart in every review."}
                  ],
                  "memory_tip": "Breaking into separate 'parts' — either physically or analytically.",
                  "synonyms": ["dismantle", "disassemble", "dissect"],
                  "antonyms": ["assemble", "build", "praise"]
                },
                {
                  "word": "take after",
                  "pos": "verb",
                  "ipa": "/teɪk ˈɑːftə/",
                  "definition": "To resemble or have characteristics of a parent or older relative.",
                  "meanings": [
                    {"num": 1, "context": "Resemblance", "sentence": "Everyone says she takes after her grandmother."}
                  ],
                  "memory_tip": "Following 'after' the traits of someone older — like their shadow walks with you.",
                  "synonyms": ["resemble", "look like", "inherit"],
                  "antonyms": ["differ from", "contrast with"]
                },
                {
                  "word": "take down",
                  "pos": "verb",
                  "ipa": "/teɪk daʊn/",
                  "definition": "To write down notes; to dismantle a structure; to defeat or humiliate someone.",
                  "meanings": [
                    {"num": 1, "context": "Note-taking", "sentence": "Please take down these points for the minutes."},
                    {"num": 2, "context": "Dismantle", "sentence": "They took down the old scaffolding last weekend."}
                  ],
                  "memory_tip": "Bringing something 'down' — from wall to floor, or from speech to paper.",
                  "synonyms": ["note", "dismantle", "demolish"],
                  "antonyms": ["put up", "build", "raise"]
                },
                {
                  "word": "take for granted",
                  "pos": "verb",
                  "ipa": "/teɪk fə ˈɡrɑːntɪd/",
                  "definition": "To fail to appreciate the value of something or someone; to assume something without questioning.",
                  "meanings": [
                    {"num": 1, "context": "Undervalue", "sentence": "Don't take your health for granted until it's gone."},
                    {"num": 2, "context": "Assume", "sentence": "He took it for granted that the meeting was cancelled."}
                  ],
                  "memory_tip": "You treat something as already 'granted' — so freely given you stop noticing its value.",
                  "synonyms": ["undervalue", "overlook", "assume"],
                  "antonyms": ["appreciate", "value", "cherish"]
                }
              ],
              "practice": {
                "question": "Which phrasal verb means 'to assume control of a business or role'?",
                "options": ["take off", "take over", "take up", "take in"],
                "answer": "take over"
              }
            }
            """;

        String comePhrasalContent = """
            {
              "title": "Phrasal Verbs with COME",
              "level": "Intermediate",
              "duration": "15 min",
              "xp": "+50 XP",
              "intro": "**COME** phrasal verbs are essential for describing arrivals, discoveries, and changes in state. They appear constantly in conversation, interviews, and professional writing. Learn them well and you will sound truly fluent.",
              "words": [
                {
                  "word": "come up",
                  "pos": "verb",
                  "ipa": "/kʌm ʌp/",
                  "definition": "To arise unexpectedly; to be mentioned or raised in conversation; to approach.",
                  "meanings": [
                    {"num": 1, "context": "Topic raised", "sentence": "Your name came up during the board meeting."},
                    {"num": 2, "context": "Unexpected event", "sentence": "Something came up at work so I can't join tonight."},
                    {"num": 3, "context": "Approach", "sentence": "A stranger came up to me and asked for directions."}
                  ],
                  "memory_tip": "A subject 'comes up' like a bubble rising to the surface of a conversation.",
                  "synonyms": ["arise", "surface", "emerge"],
                  "antonyms": ["disappear", "drop", "sink"]
                },
                {
                  "word": "come across",
                  "pos": "verb",
                  "ipa": "/kʌm əˈkrɒs/",
                  "definition": "To find or meet by chance; to make an impression on others.",
                  "meanings": [
                    {"num": 1, "context": "Discovery", "sentence": "I came across this old photo while tidying up."},
                    {"num": 2, "context": "Impression", "sentence": "She comes across as very confident in interviews."}
                  ],
                  "memory_tip": "Stumbling 'across' a path — finding something you did not plan to.",
                  "synonyms": ["discover", "encounter", "seem"],
                  "antonyms": ["miss", "overlook", "search for"]
                },
                {
                  "word": "come back",
                  "pos": "verb",
                  "ipa": "/kʌm bæk/",
                  "definition": "To return to a place, person, or topic; to recover lost popularity or memory.",
                  "meanings": [
                    {"num": 1, "context": "Return", "sentence": "She came back from London with great stories."},
                    {"num": 2, "context": "Memory", "sentence": "The words of that old song slowly came back to me."}
                  ],
                  "memory_tip": "Retracing your steps 'back' to the starting point.",
                  "synonyms": ["return", "reappear", "recover"],
                  "antonyms": ["leave", "depart", "forget"]
                },
                {
                  "word": "come out",
                  "pos": "verb",
                  "ipa": "/kʌm aʊt/",
                  "definition": "To be released or published; to reveal a secret; to emerge from a difficult situation.",
                  "meanings": [
                    {"num": 1, "context": "Release", "sentence": "Her new album comes out next Friday."},
                    {"num": 2, "context": "Result", "sentence": "The report came out better than we expected."},
                    {"num": 3, "context": "Reveal", "sentence": "The truth finally came out at the hearing."}
                  ],
                  "memory_tip": "Like a butterfly coming 'out' of a cocoon — something previously hidden is now visible.",
                  "synonyms": ["emerge", "reveal", "publish"],
                  "antonyms": ["hide", "conceal", "withhold"]
                },
                {
                  "word": "come down",
                  "pos": "verb",
                  "ipa": "/kʌm daʊn/",
                  "definition": "To decrease in price or level; to fall; to be passed down through generations.",
                  "meanings": [
                    {"num": 1, "context": "Price drop", "sentence": "Petrol prices finally came down last month."},
                    {"num": 2, "context": "Illness", "sentence": "He came down with a bad fever after the trip."}
                  ],
                  "memory_tip": "Moving 'down' from a higher position — prices, temperatures, or even moods.",
                  "synonyms": ["decrease", "fall", "drop"],
                  "antonyms": ["rise", "go up", "increase"]
                },
                {
                  "word": "come up with",
                  "pos": "verb",
                  "ipa": "/kʌm ʌp wɪð/",
                  "definition": "To think of or produce an idea, solution, or plan.",
                  "meanings": [
                    {"num": 1, "context": "Idea", "sentence": "She came up with a brilliant marketing strategy."},
                    {"num": 2, "context": "Solution", "sentence": "The team came up with a fix for the critical bug."}
                  ],
                  "memory_tip": "Pulling an idea 'up' from inside your mind — like drawing water from a well.",
                  "synonyms": ["devise", "invent", "propose"],
                  "antonyms": ["fail to find", "overlook"]
                },
                {
                  "word": "come along",
                  "pos": "verb",
                  "ipa": "/kʌm əˈlɒŋ/",
                  "definition": "To accompany someone; to make progress; to arrive or appear.",
                  "meanings": [
                    {"num": 1, "context": "Accompany", "sentence": "You should come along to the workshop — it's free!"},
                    {"num": 2, "context": "Progress", "sentence": "How is your new project coming along?"}
                  ],
                  "memory_tip": "Walking 'along' side by side — joining someone on their journey.",
                  "synonyms": ["accompany", "progress", "join"],
                  "antonyms": ["stay behind", "stagnate"]
                },
                {
                  "word": "come in",
                  "pos": "verb",
                  "ipa": "/kʌm ɪn/",
                  "definition": "To enter a room or building; to be received (money, information); to finish a race.",
                  "meanings": [
                    {"num": 1, "context": "Enter", "sentence": "Please come in — make yourself comfortable."},
                    {"num": 2, "context": "Finance", "sentence": "When does the monthly salary come in?"}
                  ],
                  "memory_tip": "Stepping 'in' through the threshold — entering a new space.",
                  "synonyms": ["enter", "arrive", "receive"],
                  "antonyms": ["go out", "exit", "leave"]
                },
                {
                  "word": "come about",
                  "pos": "verb",
                  "ipa": "/kʌm əˈbaʊt/",
                  "definition": "To happen or occur, especially unexpectedly.",
                  "meanings": [
                    {"num": 1, "context": "Occurrence", "sentence": "How did this opportunity come about so suddenly?"}
                  ],
                  "memory_tip": "A situation moves 'about' into existence — it comes to be.",
                  "synonyms": ["happen", "occur", "arise"],
                  "antonyms": ["prevent", "stop", "block"]
                },
                {
                  "word": "come forward",
                  "pos": "verb",
                  "ipa": "/kʌm ˈfɔːwəd/",
                  "definition": "To present oneself, especially to offer help or provide information.",
                  "meanings": [
                    {"num": 1, "context": "Volunteer", "sentence": "Several witnesses came forward after the accident."},
                    {"num": 2, "context": "Information", "sentence": "We need someone to come forward with new evidence."}
                  ],
                  "memory_tip": "Moving 'forward' to take a visible, courageous position.",
                  "synonyms": ["volunteer", "step up", "present oneself"],
                  "antonyms": ["hide", "stay back", "withdraw"]
                },
                {
                  "word": "come off",
                  "pos": "verb",
                  "ipa": "/kʌm ɒf/",
                  "definition": "To succeed or turn out as planned; to fall or detach from a surface.",
                  "meanings": [
                    {"num": 1, "context": "Success", "sentence": "Did the presentation come off well in the end?"},
                    {"num": 2, "context": "Detach", "sentence": "The handle came off the door when I pulled it."}
                  ],
                  "memory_tip": "Something moves 'off' its position — either succeeding by leaving its original state or physically detaching.",
                  "synonyms": ["succeed", "detach", "fall off"],
                  "antonyms": ["fail", "stay on", "attach"]
                },
                {
                  "word": "come round",
                  "pos": "verb",
                  "ipa": "/kʌm raʊnd/",
                  "definition": "To visit someone informally; to regain consciousness; to change one's opinion.",
                  "meanings": [
                    {"num": 1, "context": "Visit", "sentence": "Do come round for tea on Sunday afternoon."},
                    {"num": 2, "context": "Consciousness", "sentence": "She came round slowly after the anaesthetic."},
                    {"num": 3, "context": "Opinion change", "sentence": "He eventually came round to our way of thinking."}
                  ],
                  "memory_tip": "Moving in a 'round' circuit — returning back to a place or a position.",
                  "synonyms": ["visit", "recover", "change mind"],
                  "antonyms": ["stay away", "faint", "remain stubborn"]
                }
              ],
              "practice": {
                "question": "Which phrasal verb means 'to think of a new idea or solution'?",
                "options": ["come across", "come up with", "come back", "come off"],
                "answer": "come up with"
              }
            }
            """;

        String goPhrasalContent = """
            {
              "title": "Phrasal Verbs with GO",
              "level": "Intermediate",
              "duration": "15 min",
              "xp": "+50 XP",
              "intro": "**GO** phrasal verbs describe movement, progress, and change. They are among the most frequently used expressions in everyday English — from casual conversation to formal presentations and interviews.",
              "words": [
                {
                  "word": "go on",
                  "pos": "verb",
                  "ipa": "/ɡəʊ ɒn/",
                  "definition": "To continue; to happen; to begin operating.",
                  "meanings": [
                    {"num": 1, "context": "Continue", "sentence": "Please go on — I am listening carefully."},
                    {"num": 2, "context": "Happen", "sentence": "What is going on in the conference room?"},
                    {"num": 3, "context": "Operating", "sentence": "The generator goes on automatically during a power cut."}
                  ],
                  "memory_tip": "Keep moving 'on' along the same track — continuing without stopping.",
                  "synonyms": ["continue", "happen", "proceed"],
                  "antonyms": ["stop", "cease", "halt"]
                },
                {
                  "word": "go off",
                  "pos": "verb",
                  "ipa": "/ɡəʊ ɒf/",
                  "definition": "To explode or fire; to sound an alarm; to leave; for food to become stale.",
                  "meanings": [
                    {"num": 1, "context": "Alarm", "sentence": "My alarm goes off at 5:30 every morning."},
                    {"num": 2, "context": "Food", "sentence": "The milk has gone off — it smells sour."},
                    {"num": 3, "context": "Dislike", "sentence": "I've completely gone off eating junk food lately."}
                  ],
                  "memory_tip": "Something departs 'off' its normal path — an alarm fires, food spoils, or you stop liking it.",
                  "synonyms": ["explode", "sound", "spoil"],
                  "antonyms": ["stay on", "remain fresh", "like"]
                },
                {
                  "word": "go over",
                  "pos": "verb",
                  "ipa": "/ɡəʊ ˈəʊvə/",
                  "definition": "To review or examine something carefully; to be well received.",
                  "meanings": [
                    {"num": 1, "context": "Review", "sentence": "Let's go over the report one more time before the meeting."},
                    {"num": 2, "context": "Reception", "sentence": "Her speech went over very well with the audience."}
                  ],
                  "memory_tip": "Looking 'over' every detail — like an eagle scanning the ground below.",
                  "synonyms": ["review", "check", "examine"],
                  "antonyms": ["ignore", "skip", "overlook"]
                },
                {
                  "word": "go through",
                  "pos": "verb",
                  "ipa": "/ɡəʊ θruː/",
                  "definition": "To experience a difficult situation; to examine something carefully; to be approved.",
                  "meanings": [
                    {"num": 1, "context": "Experience", "sentence": "She went through a very tough phase last year."},
                    {"num": 2, "context": "Examine", "sentence": "Let me go through the contract clause by clause."},
                    {"num": 3, "context": "Approval", "sentence": "The proposal finally went through after months of review."}
                  ],
                  "memory_tip": "Pushing 'through' a thick forest — it takes effort but you reach the other side.",
                  "synonyms": ["experience", "examine", "approve"],
                  "antonyms": ["avoid", "skip", "reject"]
                },
                {
                  "word": "go back",
                  "pos": "verb",
                  "ipa": "/ɡəʊ bæk/",
                  "definition": "To return to a place, time, or activity; to have a long shared history.",
                  "meanings": [
                    {"num": 1, "context": "Return", "sentence": "We should go back to basics with this strategy."},
                    {"num": 2, "context": "History", "sentence": "Our friendship goes back more than twenty years."}
                  ],
                  "memory_tip": "Retracing steps 'back'wards to an earlier point — in time or space.",
                  "synonyms": ["return", "revert", "trace back"],
                  "antonyms": ["move forward", "progress", "advance"]
                },
                {
                  "word": "go ahead",
                  "pos": "verb",
                  "ipa": "/ɡəʊ əˈhed/",
                  "definition": "To proceed with a plan; to give permission to begin.",
                  "meanings": [
                    {"num": 1, "context": "Permission", "sentence": "Go ahead — the floor is yours for the presentation."},
                    {"num": 2, "context": "Proceed", "sentence": "The concert will go ahead despite the rain."}
                  ],
                  "memory_tip": "Moving 'ahead' of the starting line — you have clearance to begin.",
                  "synonyms": ["proceed", "advance", "continue"],
                  "antonyms": ["stop", "cancel", "hold back"]
                },
                {
                  "word": "go up",
                  "pos": "verb",
                  "ipa": "/ɡəʊ ʌp/",
                  "definition": "To increase in price, level, or value; to approach someone; to be built.",
                  "meanings": [
                    {"num": 1, "context": "Increase", "sentence": "Fuel prices have gone up sharply this quarter."},
                    {"num": 2, "context": "Construction", "sentence": "New apartments are going up along the riverside."}
                  ],
                  "memory_tip": "Numbers climbing 'up' the scale — like a thermometer rising in summer.",
                  "synonyms": ["rise", "increase", "be built"],
                  "antonyms": ["fall", "decrease", "drop"]
                },
                {
                  "word": "go down",
                  "pos": "verb",
                  "ipa": "/ɡəʊ daʊn/",
                  "definition": "To decrease; to be recorded in history; to be received in a certain way.",
                  "meanings": [
                    {"num": 1, "context": "Decrease", "sentence": "Interest rates went down for the third time this year."},
                    {"num": 2, "context": "History", "sentence": "This victory will go down in the history books."},
                    {"num": 3, "context": "Reception", "sentence": "His joke didn't go down well in the boardroom."}
                  ],
                  "memory_tip": "Descending 'down'ward — in price, in records, or in how people react.",
                  "synonyms": ["decrease", "fall", "be remembered"],
                  "antonyms": ["rise", "go up", "be forgotten"]
                },
                {
                  "word": "go out",
                  "pos": "verb",
                  "ipa": "/ɡəʊ aʊt/",
                  "definition": "To leave the house for social activities; for a light or fire to stop burning; to be in a romantic relationship.",
                  "meanings": [
                    {"num": 1, "context": "Social", "sentence": "We went out for dinner to celebrate the promotion."},
                    {"num": 2, "context": "Light", "sentence": "The power went out during the storm last night."}
                  ],
                  "memory_tip": "Moving 'out' of your usual space — leaving the indoor world behind.",
                  "synonyms": ["leave", "date", "extinguish"],
                  "antonyms": ["stay in", "light up", "break up"]
                },
                {
                  "word": "go along with",
                  "pos": "verb",
                  "ipa": "/ɡəʊ əˈlɒŋ wɪð/",
                  "definition": "To agree with or support a plan, idea, or person.",
                  "meanings": [
                    {"num": 1, "context": "Agreement", "sentence": "I will go along with whatever the team decides."}
                  ],
                  "memory_tip": "Walking 'along with' someone — moving in the same direction, agreeing.",
                  "synonyms": ["agree", "support", "comply"],
                  "antonyms": ["oppose", "refuse", "resist"]
                },
                {
                  "word": "go for",
                  "pos": "verb",
                  "ipa": "/ɡəʊ fɔː/",
                  "definition": "To choose or attempt something; to try to achieve a goal.",
                  "meanings": [
                    {"num": 1, "context": "Choice", "sentence": "I'll go for the grilled salmon, please."},
                    {"num": 2, "context": "Attempt", "sentence": "Just go for it — you have nothing to lose!"}
                  ],
                  "memory_tip": "Directing yourself 'for' a target — aiming and attempting.",
                  "synonyms": ["choose", "attempt", "pursue"],
                  "antonyms": ["avoid", "decline", "give up"]
                },
                {
                  "word": "go without",
                  "pos": "verb",
                  "ipa": "/ɡəʊ wɪˈðaʊt/",
                  "definition": "To manage or survive without having something.",
                  "meanings": [
                    {"num": 1, "context": "Deprivation", "sentence": "During the lockdown many families went without luxuries."},
                    {"num": 2, "context": "Common sense", "sentence": "It goes without saying that safety comes first."}
                  ],
                  "memory_tip": "Moving forward 'without' something — surviving in its absence.",
                  "synonyms": ["do without", "forgo", "abstain"],
                  "antonyms": ["have", "enjoy", "possess"]
                }
              ],
              "practice": {
                "question": "Which phrasal verb means 'to review something carefully before a meeting'?",
                "options": ["go on", "go off", "go over", "go through"],
                "answer": "go over"
              }
            }
            """;

        // ── Register all Vocabulary lessons ────────────────────────────────
        VocabularyLessonSeeder.seed(
            lessonRepository,
            getActiveContent,
            makePhrasalContent,
            takePhrasalContent,
            comePhrasalContent,
            goPhrasalContent
        );
    }

    private String getPlaceholderContent(String title) {
        return "{\n" +
                "  \"title\": \"" + title + "\",\n" +
                "  \"level\": \"Intermediate\",\n" +
                "  \"duration\": \"10 min\",\n" +
                "  \"xp\": \"+30 XP\",\n" +
                "  \"intro\": \"### " + title + "\\n\\nWelcome to the lesson on **" + title + "**! This lesson content will be updated soon. Stay tuned!\",\n" +
                "  \"words\": [\n" +
                "    {\n" +
                "      \"word\": \"Example Word\",\n" +
                "      \"pos\": \"noun\",\n" +
                "      \"ipa\": \"/ɪɡˈzɑːm.pəl/\",\n" +
                "      \"definition\": \"A representative form or pattern.\",\n" +
                "      \"meanings\": [\n" +
                "        {\"num\": 1, \"context\": \"Usage\", \"sentence\": \"This is a sample sentence for illustration.\"}\n" +
                "      ],\n" +
                "      \"memory_tip\": \"Remember this word by practicing daily.\",\n" +
                "      \"synonyms\": [\"model\", \"pattern\"],\n" +
                "      \"antonyms\": [\"exception\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"practice\": {\n" +
                "    \"question\": \"Which word is a synonym of 'Example Word'?\",\n" +
                "    \"options\": [\"exception\", \"model\", \"different\", \"opposite\"],\n" +
                "    \"answer\": \"model\"\n" +
                "  }\n" +
                "}";
    }

    private void seedPronunciationLessons(java.util.Set<String> existingKeys) {
        String thSoundContent = """
            {
              "title": "TH Sound (Voiced & Unvoiced)",
              "phoneme": "/θ/ & /ð/",
              "description": "Place your tongue tip gently between your upper and lower front teeth. Blow air through the gap.",
              "mouth_image": "https://lh3.googleusercontent.com/aida-public/AB6AXuBxPgezovns-F6_5Zdee6V6WgMyNRjAAJPSuhJnelWhO26ubgmcqelgY2u6NE4HDjwNVTWViPfH_hK-dcByTQ3rvCpKIXbwT2ijm-wkjf4hzJgj6yhGHw-5gbmE3a2K0EItBDLUTOxuIojaOVqtptO58TchNm1Xr3ZO6MKRAaEJiVfWY3ruu8zu4KYsKrMsuOo-OJfOsnKO91zeyYXIOeVDDkdIOLIctdoIDkLSChfO5Sgdxg14LY6-",
              "intro": "### The Voiced and Unvoiced TH Sounds\\n\\nThe 'TH' sounds are some of the most unique and challenging sounds in the English language. Unlike many other sounds, they require you to place your tongue tip between your teeth.\\n\\nThere are two types of 'TH' sounds:\\n\\n1. **Unvoiced TH /θ/:** No vibration in your vocal cords. It is just breath passing through your teeth (e.g., *think, thin, birthday, bath*).\\n2. **Voiced TH /ð/:** Your vocal cords vibrate while you make the sound (e.g., *the, this, mother, breathe*).",
              "spotlight": [
                {
                  "title": "COMMON PITFALL: THE HARD 'T' OR 'D'",
                  "desc": "Many speakers substitute unvoiced /θ/ with a hard /t/ (e.g., saying 'tink' instead of 'think') or voiced /ð/ with a hard /d/ (e.g., saying 'dis' instead of 'this'). Keep your tongue flat and blow air; do not build up pressure.",
                  "example": "Try saying 'this' /ðɪs/ with a soft buzz instead of 'dis'."
                },
                {
                  "title": "TAMIL SPEAKER SPECIFIC: DENTAL STOPS",
                  "desc": "Tamil speakers often substitute the 'TH' with the retroflex or dental stops found in Tamil (like the 'th' in 'patham'). To correct this, ensure the tongue is extended and visible between your teeth, rather than hitting the roof of the mouth.",
                  "example": "Look in a mirror and make sure the tip of your tongue is visible between your teeth."
                }
              ],
              "words": [
                {"word": "think", "ipa": "/θɪŋk/", "meaning": "Unvoiced: To have an opinion or reflect on something"},
                {"word": "theory", "ipa": "/ˈθɪə.ri/", "meaning": "Unvoiced: A formal statement or idea"},
                {"word": "thirty", "ipa": "/ˈθɜː.ti/", "meaning": "Unvoiced: The number 30"},
                {"word": "breath", "ipa": "/breθ/", "meaning": "Unvoiced: The air taken into the lungs"},
                {"word": "mouth", "ipa": "/maʊθ/", "meaning": "Unvoiced: The opening through which we speak and eat"},
                {"word": "this", "ipa": "/ðɪs/", "meaning": "Voiced: Referring to a specific nearby object or idea"},
                {"word": "mother", "ipa": "/ˈmʌð.ər/", "meaning": "Voiced: A female parent"},
                {"word": "breathe", "ipa": "/briːð/", "meaning": "Voiced: The action of inhaling and exhaling"},
                {"word": "weather", "ipa": "/ˈweð.ər/", "meaning": "Voiced: The state of the atmosphere"},
                {"word": "smooth", "ipa": "/smuːð/", "meaning": "Voiced: Having an even and regular surface"}
              ],
              "target_phrase": "Think of thirty things that mother breathed in smooth weather.",
              "minimal_pairs_listen": [
                {"word1": "think", "word2": "sink", "correct": "think", "ipa1": "/θɪŋk/", "ipa2": "/sɪŋk/"},
                {"word1": "thin", "word2": "tin", "correct": "thin", "ipa1": "/θɪn/", "ipa2": "/tɪn/"},
                {"word1": "thought", "word2": "taught", "correct": "thought", "ipa1": "/θɔːt/", "ipa2": "/tɔːt/"},
                {"word1": "they", "word2": "day", "correct": "they", "ipa1": "/ðeɪ/", "ipa2": "/deɪ/"},
                {"word1": "breathe", "word2": "breed", "correct": "breathe", "ipa1": "/briːð/", "ipa2": "/briːd/"}
              ],
              "tongue_twister": {
                "text": "The thirty-three thankful thieves thought that they thrilled their mother.",
                "slow_speed": 0.7,
                "normal_speed": 1.0,
                "fast_speed": 1.3
              },
              "mini_quiz": [
                {
                  "question": "Which word contains the VOICED /ð/ sound?",
                  "options": ["healthy", "mother", "nothing", "mouth"],
                  "answer": "mother"
                },
                {
                  "question": "Which word contains the UNVOICED /θ/ sound?",
                  "options": ["brother", "they", "breath", "breathe"],
                  "answer": "breath"
                }
              ]
            }
            """;

        PronunciationLessonSeeder.seed(lessonRepository, thSoundContent);
    }
}
