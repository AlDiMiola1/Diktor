package com.example.data.catalog

import com.example.model.CourseDayTemplate
import com.example.model.CourseProgram
import com.example.model.CourseStep
import com.example.model.DictionTable
import com.example.model.Exercise
import com.example.model.ExerciseModule
import com.example.model.ExerciseVizType
import com.example.model.TeleprompterScript
import com.example.model.TongueTwister
import kotlin.math.max

object DiktorCatalog {

    val EXERCISES: List<Exercise> = listOf(
        // Модуль 1: Зажимы (6)
        Exercise(
            id = "hidden_yawn",
            num = 1,
            module = ExerciseModule.CLAMPS,
            title = "Скрытый зевок",
            shortDesc = "Опускание гортани и подъем мягкого нёба по фазам",
            durationLabel = "45с",
            vizType = ExerciseVizType.HIDDEN_YAWN,
            defaultDurationSec = 45,
            steps = listOf(
                "Сомкните губы, слегка разомкните зубы и сделайте вдох носом с ощущением начала зевка.",
                "Следите за схемой: мягкое нёбо поднимается вверх, а гортань плавно опускается вниз.",
                "Удерживайте объемную глотку 3 секунды и мягко выдохните."
            )
        ),
        Exercise(
            id = "horse_lips",
            num = 2,
            module = ExerciseModule.CLAMPS,
            title = "Лошадка",
            shortDesc = "Сброс напряжения губами («тпр-р-р») сверху вниз",
            durationLabel = "30с",
            vizType = ExerciseVizType.HORSE_LIPS,
            defaultDurationSec = 30,
            steps = listOf(
                "Расслабьте губы и пустите ровную струю воздуха, заставляя губы вибрировать.",
                "Добавьте голос и плавно скользите по тону сверху вниз, снимая зажим со связок."
            )
        ),
        Exercise(
            id = "jaw_release",
            num = 3,
            module = ExerciseModule.CLAMPS,
            title = "Точечный релиз и мертвая челюсть",
            shortDesc = "Массаж жевательных мышц + падение челюсти на «ма-а-а»",
            durationLabel = "50с",
            vizType = ExerciseVizType.JAW_RELEASE,
            defaultDurationSec = 50,
            steps = listOf(
                "15 сек: круговыми движениями разомните жевательную мышцу под левой скулой.",
                "15 сек: перейдите на правую скулу.",
                "20 сек: отпустите нижнюю челюсть вниз под собственным весом и протяните расслабленное «ма-а-а»."
            )
        ),
        Exercise(
            id = "neck_pendulum",
            num = 4,
            module = ExerciseModule.CLAMPS,
            title = "Шейный маятник",
            shortDesc = "Перекаты подбородка по груди для снятия зажима шеи",
            durationLabel = "8 повт.",
            vizType = ExerciseVizType.NECK_PENDULUM,
            defaultDurationSec = 40,
            steps = listOf(
                "Опустите подбородок к груди и перекатывайте его от одного плеча к другому (без запрокидывания головы назад).",
                "Отмечайте каждый повтор на счетчике до 8 раз."
            )
        ),
        Exercise(
            id = "tongue_root_drop",
            num = 5,
            module = ExerciseModule.CLAMPS,
            title = "Сброс корня языка",
            shortDesc = "Вытягивание языка вперед на выдохе для освобождения глотки",
            durationLabel = "40с",
            vizType = ExerciseVizType.TONGUE_ROOT_DROP,
            defaultDurationSec = 40,
            steps = listOf(
                "Свободно высуньте язык вперед к подбородку на 3 секунды, полностью расслабляя корень языка.",
                "На выдохе мягко протяните открытый звук «А-а-а», затем уберите язык за нижние зубы."
            )
        ),
        Exercise(
            id = "ragdoll_shoulders",
            num = 6,
            module = ExerciseModule.CLAMPS,
            title = "Тряпичная кукла (Сброс плеч)",
            shortDesc = "Сброс зажима плечевого пояса на резком выдохе «Ха-а»",
            durationLabel = "35с",
            vizType = ExerciseVizType.RAGDOLL_SHOULDERS,
            defaultDurationSec = 35,
            steps = listOf(
                "На вдохе сильно подтяните плечи к ушам и задержите напряжение на 2 секунды.",
                "С громким расслабленным выдохом «Ха-а-а» бросьте плечи вниз, освобождая грудную клетку."
            )
        ),

        // Модуль 2: Дыхание (6)
        Exercise(
            id = "candle_breath",
            num = 7,
            module = ExerciseModule.BREATH,
            title = "Свеча",
            shortDesc = "Пламя от микрофона — держите угол 45° ровным «С-с-с»",
            durationLabel = "Микр.",
            vizType = ExerciseVizType.CANDLE_BREATH,
            defaultDurationSec = 50,
            steps = listOf(
                "Включите микрофон (или используйте ручной слайдер-тест) и сделайте глубокий вдох нижними ребрами.",
                "Выдыхайте ровным «С-с-с», удерживая пламя в зеленой зоне под углом 45° без рывков."
            )
        ),
        Exercise(
            id = "pump_strelnikova",
            num = 8,
            module = ExerciseModule.BREATH,
            title = "Насос по Стрельниковой",
            shortDesc = "Короткий шумный вдох носом в темпе 1 вдох/сек (4 × 8)",
            durationLabel = "4 × 8",
            vizType = ExerciseVizType.PUMP_STRELNIKOVA,
            defaultDurationSec = 45,
            steps = listOf(
                "На легком полупоклоне делайте короткий, шумный вдох носом в такт пульсатору (1 раз в секунду).",
                "Выдох уходит пассивно и бесшумно через рот. Выполните 4 серии по 8 вдохов."
            )
        ),
        Exercise(
            id = "dog_breath",
            num = 9,
            module = ExerciseModule.BREATH,
            title = "Собачка",
            shortDesc = "Импульсы диафрагмой на быстрых «ха-ха-ха»",
            durationLabel = "45с",
            vizType = ExerciseVizType.DOG_BREATH,
            defaultDurationSec = 45,
            steps = listOf(
                "Плечи неподвижны. Делайте отрывистые толчки передней стенкой живота на звуке «ха-ха-ха».",
                "Синхронизируйтесь с движением диафрагмы на экране."
            )
        ),
        Exercise(
            id = "egorki_33",
            num = 10,
            module = ExerciseModule.BREATH,
            title = "Счет 33 Егорок",
            shortDesc = "Счетчик Егорок на одном выдохе с записью рекорда",
            durationLabel = "Рекорд",
            vizType = ExerciseVizType.EGORKI_33,
            defaultDurationSec = 45,
            steps = listOf(
                "Сделайте глубокий вдох нижними ребрами и нажимайте кнопку на каждого произнесенного Егорку на одном выдохе.",
                "Ваш максимальный результат автоматически сохранится в личный рекорд."
            )
        ),
        Exercise(
            id = "box_breath",
            num = 11,
            module = ExerciseModule.BREATH,
            title = "Квадрат диктора (4-4-4-4)",
            shortDesc = "Контроль объема легких и плавности опоры по 4 фазам",
            durationLabel = "64с",
            vizType = ExerciseVizType.BOX_BREATH,
            defaultDurationSec = 64,
            steps = listOf(
                "4 сек: плавный вдох в нижние ребра (плечи неподвижны).",
                "4 сек: мягкая фиксация воздуха без зажима в горле.",
                "4 сек: плотный ровный выдох на звуке «Ф-ф-ф» → 4 сек пауза."
            )
        ),
        Exercise(
            id = "stepped_exhale",
            num = 12,
            module = ExerciseModule.BREATH,
            title = "Ступенчатый выдох «С-С-С»",
            shortDesc = "Порционный сброс воздуха с фиксацией ребер на 4 такта",
            durationLabel = "45с",
            vizType = ExerciseVizType.STEPPED_EXHALE,
            defaultDurationSec = 45,
            steps = listOf(
                "Сделайте глубокий вдох и выдыхайте плотным звуком «С!» четырьмя четкими порциями.",
                "Между толчками не добирайте воздух — держите ребра раскрытыми."
            )
        ),

        // Модуль 3: Резонаторы и Штробас (8)
        Exercise(
            id = "tarzan_chest",
            num = 13,
            module = ExerciseModule.RESONATORS,
            title = "Тарзан (Грудное мычание)",
            shortDesc = "Низкое «М-м-м» с легким простукиванием грудины",
            durationLabel = "40с",
            vizType = ExerciseVizType.TARZAN_CHEST,
            defaultDurationSec = 40,
            steps = listOf(
                "Сомкните губы, разомкните зубы и протяните низкий звук «М-м-м».",
                "Легко простукивайте грудину кончиками пальцев, усиливая вибрацию в грудном резонаторе."
            )
        ),
        Exercise(
            id = "cry_sigh",
            num = 14,
            module = ExerciseModule.RESONATORS,
            title = "Крик-стон (Посыл вдаль)",
            shortDesc = "Дуга полета звука от переносицы вперед и вниз в грудь",
            durationLabel = "40с",
            vizType = ExerciseVizType.CRY_SIGH,
            defaultDurationSec = 40,
            steps = listOf(
                "Начните с легкого высокого стона в переносице.",
                "Переведите звук по дуге вперед и глубоко вниз в грудь на слоге «Ма-а-а-м»."
            )
        ),
        Exercise(
            id = "mask_focus",
            num = 15,
            module = ExerciseModule.RESONATORS,
            title = "Фокусировка в маску (Н-н-н → Ми-мэ)",
            shortDesc = "Перевод вибрации: переносица → губы → грудной объем",
            durationLabel = "54с",
            vizType = ExerciseVizType.MASK_FOCUS,
            defaultDurationSec = 54,
            steps = listOf(
                "Направьте вибрацию «Н-н-н» в переносицу, затем «М-м-м» на губы и раскройте звук в грудь на «Ми-мэ-ма-мо-му»."
            )
        ),
        Exercise(
            id = "vocal_fry",
            num = 16,
            module = ExerciseModule.RESONATORS,
            isFry = true,
            title = "Штробас с выходом в тон",
            shortDesc = "5с треск связок на минимуме воздуха → 10с глубокий «А-а-а»",
            durationLabel = "45с",
            vizType = ExerciseVizType.VOCAL_FRY,
            defaultDurationSec = 45,
            steps = listOf(
                "5 сек: расслабленное потрескивание связок (штробас) на минимальном выдохе без давления.",
                "10 сек: плавный переход из треска в ровный глубокий гласный «А-а-а»."
            )
        ),
        Exercise(
            id = "vocal_fry_morning",
            num = 17,
            module = ExerciseModule.RESONATORS,
            isFry = true,
            title = "Закрытый штробас (Массаж связок)",
            shortDesc = "Тихое «потрескивание» с сомкнутыми губами для снятия сипа",
            durationLabel = "45с",
            vizType = ExerciseVizType.VOCAL_FRY_MORNING,
            defaultDurationSec = 45,
            steps = listOf(
                "Сомкните губы, полностью расслабьте шею и челюсть.",
                "На самом минимальном выдохе извлеките ровный низкий треск связок (как утренний сонный голос) без напряжения горла."
            )
        ),
        Exercise(
            id = "vocal_fry_glissando",
            num = 18,
            module = ExerciseModule.RESONATORS,
            isFry = true,
            title = "Глиссандо: Штробас → Грудной бас",
            shortDesc = "Скольжение из сухого треска через «М-м-м» в бархатный низ",
            durationLabel = "50с",
            vizType = ExerciseVizType.VOCAL_FRY_GLISSANDO,
            defaultDurationSec = 50,
            steps = listOf(
                "Начните с тихого штробаса (3 сек), затем плавно добавьте опору и перейдите в густое грудное «М-м-м» (4 сек).",
                "Раскройте рот на низком бархатном «М-А-А-М» (5 сек), сохраняя ощущение треска в основе звука."
            )
        ),
        Exercise(
            id = "vocal_fry_syllables",
            num = 19,
            module = ExerciseModule.RESONATORS,
            isFry = true,
            title = "Слоговой штробас «БРА-БРО-БРУ»",
            shortDesc = "Зацепка низкого регистра через треск в начале каждого слога",
            durationLabel = "45с",
            vizType = ExerciseVizType.VOCAL_FRY_SYLLABLES,
            defaultDurationSec = 45,
            steps = listOf(
                "Произносите каждый слог по очереди, начиная гласный с легкого «подтрескивания» связок в самом низу диапазона.",
                "Не давите на связки громкостью — пусть звук вибрирует в грудине."
            )
        ),
        Exercise(
            id = "vocal_fry_phrases",
            num = 20,
            module = ExerciseModule.RESONATORS,
            isFry = true,
            title = "Штробас-атака на дикторских фразах",
            shortDesc = "Мягкий вход в низкий тембр на ключевых словах трейлера",
            durationLabel = "60с",
            vizType = ExerciseVizType.VOCAL_FRY_PHRASES,
            defaultDurationSec = 60,
            steps = listOf(
                "Перед выделенным словом сделайте микропаузу, зацепите звук легким штробасом и опустите фразу в грудь.",
                "Переключайте 4 тренировочные фразы ниже."
            )
        ),

        // Модуль 4: Артикуляция (6)
        Exercise(
            id = "cork_training",
            num = 21,
            module = ExerciseModule.ARTICULATION,
            title = "Пробка",
            shortDesc = "60с чтение с препятствием в зубах → 30с чистое чтение",
            durationLabel = "90с",
            vizType = ExerciseVizType.CORK_TRAINING,
            defaultDurationSec = 90,
            steps = listOf(
                "Этап 1 (60 сек): зажмите пробку или фалангу согнутого пальца между зубами и четко проговаривайте текст.",
                "Этап 2 (30 сек): уберите препятствие и прочитайте текст чисто."
            )
        ),
        Exercise(
            id = "tongue_cleaning",
            num = 22,
            module = ExerciseModule.ARTICULATION,
            title = "Чистка зубов и уколы языком",
            shortDesc = "Круговое вращение языка по деснам при сомкнутых губах",
            durationLabel = "40с",
            vizType = ExerciseVizType.TONGUE_CLEANING,
            defaultDurationSec = 40,
            steps = listOf(
                "Сомкните губы и с усилием проведите кончиком языка по верхним и нижним деснам 10 раз по часовой стрелке и 10 против.",
                "Затем сделайте по 10 плотных уколов языком в левую и правую щеку."
            )
        ),
        Exercise(
            id = "lips_trumpet_smile",
            num = 23,
            module = ExerciseModule.ARTICULATION,
            title = "Трубочка — Улыбка — Рупор",
            shortDesc = "Чередование крайних позиций губ каждые 2 секунды",
            durationLabel = "45с",
            vizType = ExerciseVizType.LIPS_TRUMPET_SMILE,
            defaultDurationSec = 45,
            steps = listOf(
                "Вытяните сомкнутые губы вперед в узкую трубочку, затем растяните в широкую улыбку без обнажения десен.",
                "Переключите губы в округлый «рупор» (как на звуке «О») с фиксацией по 2 секунды."
            )
        ),
        Exercise(
            id = "syllable_matrices",
            num = 24,
            module = ExerciseModule.ARTICULATION,
            title = "Слоговые матрицы БТК-ПТК",
            shortDesc = "Чеканка взрывных сочетаний под метроном",
            durationLabel = "50с",
            vizType = ExerciseVizType.SYLLABLE_MATRICES,
            defaultDurationSec = 50,
            steps = listOf(
                "Запустите метроном слогов и четко пробивайте каждое сочетание губами и кончиком языка.",
                "Следите, чтобы гласный после согласных звучал открыто и в груди."
            )
        ),
        Exercise(
            id = "tongue_clicks",
            num = 25,
            module = ExerciseModule.ARTICULATION,
            title = "Щелчки языком (Цоканье)",
            shortDesc = "Растяжка подъязычной уздечки в разных позициях губ",
            durationLabel = "40с",
            vizType = ExerciseVizType.TONGUE_CLICKS,
            defaultDurationSec = 40,
            steps = listOf(
                "Присасывайте широкий язык к верхнему нёбу и звонко щелкайте им, удерживая нижнюю челюсть открытой.",
                "Меняйте положение губ по подсказкам на экране: улыбка → трубочка → рупор."
            )
        ),
        Exercise(
            id = "explosive_consonants",
            num = 26,
            module = ExerciseModule.ARTICULATION,
            title = "Взрывные согласные П-Т-К / Б-Д-Г",
            shortDesc = "Упругий отскок губ и языка на глухих и звонких парах",
            durationLabel = "45с",
            vizType = ExerciseVizType.EXPLOSIVE_CONSONANTS,
            defaultDurationSec = 45,
            steps = listOf(
                "Произносите тройки согласных резко и упруго, подавая плотный импульс диафрагмой.",
                "Не добавляйте лишний призвук «Э» после согласных — работайте чисто смычкой."
            )
        ),

        // Модуль 5: Интонация (6)
        Exercise(
            id = "intonation_swing",
            num = 27,
            module = ExerciseModule.INTONATION,
            title = "Интонационные качели",
            shortDesc = "Чтение фразы в 3 регистрах: верхний, средний, грудной бас",
            durationLabel = "45с",
            vizType = ExerciseVizType.INTONATION_SWING,
            defaultDurationSec = 45,
            steps = listOf(
                "Произнесите тренировочную фразу сначала в легком верхнем регистре, затем в нейтральном среднем.",
                "На третьем шаге опустите фразу в глубокий грудной регистр с уверенной точкой."
            )
        ),
        Exercise(
            id = "semantic_magnet",
            num = 28,
            module = ExerciseModule.INTONATION,
            title = "Смысловой магнит (4 дубля)",
            shortDesc = "Перенос логического ударения и микропаузы внутри фразы",
            durationLabel = "45с",
            vizType = ExerciseVizType.SEMANTIC_MAGNET,
            defaultDurationSec = 45,
            steps = listOf(
                "Переключайте 4 дубля и делайте короткую паузу перед выделенным словом.",
                "Выделяйте ударное слово не криком, а замедлением и глубиной грудного тона."
            )
        ),
        Exercise(
            id = "markup_reading",
            num = 29,
            module = ExerciseModule.INTONATION,
            title = "Чтение по дикторской разметке",
            shortDesc = "Практика пауз |, || и акцентов на студийном телесуфлере",
            durationLabel = "Эфир",
            vizType = ExerciseVizType.MARKUP_READING,
            defaultDurationSec = 60,
            steps = listOf(
                "Перейдите в Студию и прочитайте выбранный сценарий, соблюдая все маркеры пауз и движения тона.",
                "Запишите дубль и проверьте оценку опоры и темпоритма."
            )
        ),
        Exercise(
            id = "sentence_drop",
            num = 30,
            module = ExerciseModule.INTONATION,
            title = "Уверенная точка в конце фразы ↘",
            shortDesc = "Посадка голоса вниз на последнем слове без потери громкости",
            durationLabel = "45с",
            vizType = ExerciseVizType.SENTENCE_DROP,
            defaultDurationSec = 45,
            steps = listOf(
                "На последнем ударном слове плавно опустите тон вниз в грудь, сохраняя плотный выдох.",
                "Избегайте вопросительного задирания интонации вверх в конце утверждений."
            )
        ),
        Exercise(
            id = "tempo_contrast",
            num = 31,
            module = ExerciseModule.INTONATION,
            title = "Контраст темпоритма",
            shortDesc = "Быстрый информационный разгон → весомое замедление на выводе",
            durationLabel = "50с",
            vizType = ExerciseVizType.TEMPO_CONTRAST,
            defaultDurationSec = 50,
            steps = listOf(
                "Первую часть фразы прочитайте энергично и собранно на одном дыхании.",
                "После знака || сделайте паузу и произнесите ключевой вывод медленно, весомо и низко."
            )
        ),
        Exercise(
            id = "dramatic_pause",
            num = 32,
            module = ExerciseModule.INTONATION,
            title = "Драматическая пауза МХАТ",
            shortDesc = "Удержание 2 секунд полной тишины перед главным словом",
            durationLabel = "45с",
            vizType = ExerciseVizType.DRAMATIC_PAUSE,
            defaultDurationSec = 45,
            steps = listOf(
                "Остановитесь перед главным словом, спокойно удержите 2 секунды тишины без суеты.",
                "Вступите на ключевом слове мягким грудным басом."
            )
        )
    )

    val COURSE_DAYS: List<CourseDayTemplate> = listOf(
        CourseDayTemplate(
            title = "Снятие зажимов и запуск опоры",
            focus = "Опускаем гортань, включаем диафрагму и грудной резонатор",
            approxMin = 6,
            steps = listOf(
                CourseStep("hidden_yawn", 45),
                CourseStep("horse_lips", 30),
                CourseStep("candle_breath", 50),
                CourseStep("tarzan_chest", 40)
            )
        ),
        CourseDayTemplate(
            title = "Грудной резонатор и мягкий штробас",
            focus = "Убираем зажим челюсти и находим глубокий бархат без натуги",
            approxMin = 6,
            steps = listOf(
                CourseStep("jaw_release", 50),
                CourseStep("vocal_fry_morning", 45),
                CourseStep("vocal_fry_glissando", 50),
                CourseStep("cry_sigh", 40)
            )
        ),
        CourseDayTemplate(
            title = "Дикция, пробка и четкость согласных",
            focus = "Активизируем губы, язык и объем легких",
            approxMin = 7,
            steps = listOf(
                CourseStep("neck_pendulum", 40),
                CourseStep("pump_strelnikova", 45),
                CourseStep("cork_training", 90),
                CourseStep("syllable_matrices", 50)
            )
        ),
        CourseDayTemplate(
            title = "Глубина баса и штробас-атака",
            focus = "Закрепляем низкий грудной регистр на слогах и звуках",
            approxMin = 6,
            steps = listOf(
                CourseStep("tongue_root_drop", 40),
                CourseStep("vocal_fry", 45),
                CourseStep("vocal_fry_syllables", 45),
                CourseStep("tarzan_chest", 40)
            )
        ),
        CourseDayTemplate(
            title = "Интонация и управление паузами",
            focus = "Учимся менять смысловые акценты и держать ровный выдох",
            approxMin = 6,
            steps = listOf(
                CourseStep("ragdoll_shoulders", 35),
                CourseStep("box_breath", 64),
                CourseStep("intonation_swing", 45),
                CourseStep("semantic_magnet", 45)
            )
        ),
        CourseDayTemplate(
            title = "Артикуляционный разгон и дыхание",
            focus = "Тренируем быстрый добор воздуха и подвижность языка",
            approxMin = 6,
            steps = listOf(
                CourseStep("dog_breath", 45),
                CourseStep("egorki_33", 45),
                CourseStep("tongue_cleaning", 40),
                CourseStep("lips_trumpet_smile", 45)
            )
        ),
        CourseDayTemplate(
            title = "Контрольный эфир и штробас-фразы",
            focus = "Применяем низкий тембр и уверенные окончания на тексте в Студии",
            approxMin = 7,
            steps = listOf(
                CourseStep("horse_lips", 30),
                CourseStep("vocal_fry_phrases", 50),
                CourseStep("sentence_drop", 45),
                CourseStep(isGoStudioAction = true)
            )
        ),
        CourseDayTemplate(
            title = "Свободная глотка и ступенчатый выдох",
            focus = "Контроль ребер, фокус звука в маску и взрывная дикция",
            approxMin = 7,
            steps = listOf(
                CourseStep("hidden_yawn", 45),
                CourseStep("stepped_exhale", 45),
                CourseStep("mask_focus", 54),
                CourseStep("explosive_consonants", 45)
            )
        ),
        CourseDayTemplate(
            title = "Плотный тембр и драматическая пауза",
            focus = "Глубокий бас + мастерство выдерживать тишину перед акцентом",
            approxMin = 6,
            steps = listOf(
                CourseStep("jaw_release", 50),
                CourseStep("vocal_fry_glissando", 50),
                CourseStep("dramatic_pause", 45),
                CourseStep("tempo_contrast", 50)
            )
        ),
        CourseDayTemplate(
            title = "Взрывная дикция и щелчки языком",
            focus = "Проработка подъязычной уздечки и четкости трудных согласных",
            approxMin = 7,
            steps = listOf(
                CourseStep("neck_pendulum", 40),
                CourseStep("tongue_clicks", 40),
                CourseStep("explosive_consonants", 45),
                CourseStep("cork_training", 90)
            )
        ),
        CourseDayTemplate(
            title = "Максимальный объем дыхания",
            focus = "Побиваем личный рекорд длинного выдоха на опоре",
            approxMin = 6,
            steps = listOf(
                CourseStep("pump_strelnikova", 45),
                CourseStep("egorki_33", 45),
                CourseStep("candle_breath", 60),
                CourseStep("tarzan_chest", 40)
            )
        ),
        CourseDayTemplate(
            title = "Глубокий бархат (Штробас-интенсив)",
            focus = "Полная проработка низкого дикторского регистра без зажимов",
            approxMin = 7,
            steps = listOf(
                CourseStep("tongue_root_drop", 40),
                CourseStep("vocal_fry_morning", 45),
                CourseStep("vocal_fry_syllables", 45),
                CourseStep("vocal_fry_phrases", 50)
            )
        ),
        CourseDayTemplate(
            title = "Интонационное мастерство диктора",
            focus = "Широкий диапазон, уверенная точка и управление темпоритмом",
            approxMin = 6,
            steps = listOf(
                CourseStep("ragdoll_shoulders", 35),
                CourseStep("box_breath", 64),
                CourseStep("intonation_swing", 45),
                CourseStep("sentence_drop", 45)
            )
        ),
        CourseDayTemplate(
            title = "Итоговый эфирный экзамен цикла",
            focus = "Комплексный разогрев + контрольная запись с оценкой ИИ-режиссера",
            approxMin = 7,
            steps = listOf(
                CourseStep("horse_lips", 30),
                CourseStep("vocal_fry", 45),
                CourseStep("syllable_matrices", 50),
                CourseStep(isGoStudioAction = true)
            )
        )
    )

    val EXPRESS_PROGRAM = CourseProgram(
        id = "express",
        dayNumber = 0,
        dayInCycle = 0,
        totalInCycle = 0,
        cycleNum = 0,
        title = "Экспресс-разогрев (4 мин)",
        shortTitle = "Экспресс-разогрев",
        focus = "Быстрая подготовка перед созвоном или записью",
        approxMin = 4,
        steps = listOf(
            CourseStep("horse_lips", 30),
            CourseStep("vocal_fry_glissando", 40),
            CourseStep("cork_training", 90),
            CourseStep(isGoStudioAction = true)
        )
    )

    fun getCourseProgramForDay(dayNumber: Int): CourseProgram {
        val safeDay = max(1, dayNumber)
        val idx = (safeDay - 1) % COURSE_DAYS.size
        val cycleNum = (safeDay - 1) / COURSE_DAYS.size + 1
        val template = COURSE_DAYS[idx]
        return CourseProgram(
            id = "daily_course",
            dayNumber = safeDay,
            dayInCycle = idx + 1,
            totalInCycle = COURSE_DAYS.size,
            cycleNum = cycleNum,
            title = "День $safeDay: ${template.title}",
            shortTitle = template.title,
            focus = template.focus,
            approxMin = template.approxMin,
            steps = template.steps
        )
    }

    fun findExercise(id: String?): Exercise? = EXERCISES.find { it.id == id }

    val BUILTIN_SCRIPTS: List<TeleprompterScript> = listOf(
        TeleprompterScript(
            id = "sc_news_1",
            genre = "news",
            genreLabel = "Новости",
            title = "Экономический обзор",
            isCustom = false,
            text = "Центральный *банк* ↗ | представил обновленный *прогноз* развития на ближайшие три года ↘. || По оценкам *аналитиков* ↗, | ключевым драйвером роста станет *внутренний* спрос | и модернизация логистических *коридоров* ↘."
        ),
        TeleprompterScript(
            id = "sc_news_2",
            genre = "news",
            genreLabel = "Новости",
            title = "Орбитальная связь",
            isCustom = false,
            text = "Российские *инженеры* ↗ | завершили испытания нового орбитального *модуля* связи ↘. || Аппарат способен передавать *данные* в высокочастотном диапазоне ↗ | даже в условиях экстремальной *полярной* погоды ↘."
        ),
        TeleprompterScript(
            id = "sc_trailer_1",
            genre = "trailer",
            genreLabel = "Реклама",
            title = "Кинотрейлер: Голос",
            isCustom = false,
            text = "Когда гаснет *свет* ↗ | и привычный мир рушится за одну секунду... ↘ || Остается только *голос* ↗, | который ведет сквозь темноту ↘. || *Этой осенью* — | история, которая изменит *всё* ↘."
        ),
        TeleprompterScript(
            id = "sc_trailer_2",
            genre = "trailer",
            genreLabel = "Реклама",
            title = "Премиум-бренд",
            isCustom = false,
            text = "Настоящая *сила* ↗ | не нуждается в лишних словах ↘. || Безупречный *баланс* формы ↗, | тишины | и абсолютного *контроля* ↘. || Создано для тех, | кто задает *ритм* ↘."
        ),
        TeleprompterScript(
            id = "sc_book_1",
            genre = "audiobook",
            genreLabel = "Аудиокнига",
            title = "Ночная набережная",
            isCustom = false,
            text = "Ночной *город* затихал медленно ↘. || Фонари на пустынной *набережной* ↗ | отражались в темной воде, | словно рассыпанные по бархату *золотые* монеты ↘. || Он остановился у парапета ↗ | и впервые за долгий день *спокойно* выдохнул ↘."
        )
    )

    val BUILTIN_TWISTERS: List<TongueTwister> = listOf(
        TongueTwister(
            id = "tw_1",
            soundTag = "Р / Л",
            difficulty = "Сложная",
            isCustom = false,
            text = "В четверг четвертого числа в четыре с четвертью часа лигурийский регулировщик регулировал в Лигурии, но тридцать три корабля лавировали, лавировали, да так и не вылавировали."
        ),
        TongueTwister(
            id = "tw_2",
            soundTag = "Р / Л",
            difficulty = "Базовая",
            isCustom = false,
            text = "Корабли лавировали, лавировали, да не вылавировали, ведь ветер с залива валил и валы вздымал, а старый боцман браво выговаривал скороговорки."
        ),
        TongueTwister(
            id = "tw_3",
            soundTag = "С / З / Ш",
            difficulty = "Сложная",
            isCustom = false,
            text = "Шла Саша по шоссе и сосала сушку, а навстречу Саше шел с сушками старушки муж — сушил сушки на шесте, чтоб не шуршали в тишине."
        ),
        TongueTwister(
            id = "tw_4",
            soundTag = "П / Т / К",
            difficulty = "Хардкор",
            isCustom = false,
            text = "От топота копыт пыль по полю летит, а протокол про протокол протоколом запротоколировали, перепротоколировали, да так и не выпротоколировали."
        ),
        TongueTwister(
            id = "tw_5",
            soundTag = "Шипящие",
            difficulty = "Сложная",
            isCustom = false,
            text = "Два щенка щека к щеке щиплют щетку в уголке, а щегол в чаще щебечет про щедрого ямщика."
        )
    )

    val BUILTIN_DICTION_TABLES: List<DictionTable> = listOf(
        DictionTable(
            id = "lvl1",
            title = "1. База (18)",
            subtitle = "Ряды: БТК • ПТК • ГДК (взрывные)",
            syllables = listOf(
                "БТКА", "БТКО", "БТКУ", "БТКЭ", "БТКИ", "БТКЫ",
                "ПТКА", "ПТКО", "ПТКУ", "ПТКЭ", "ПТКИ", "ПТКЫ",
                "ГДКА", "ГДКО", "ГДКУ", "ГДКЭ", "ГДКИ", "ГДКЫ"
            )
        ),
        DictionTable(
            id = "lvl2",
            title = "2. Средний (30)",
            subtitle = "Ряды: РЛА • ЛРА • ШТА • ЖДА • МНА (30 слогов)",
            syllables = listOf(
                "РЛА", "РЛО", "РЛУ", "РЛЭ", "РЛИ", "РЛЫ",
                "ЛРА", "ЛРО", "ЛРУ", "ЛРЭ", "ЛРИ", "ЛРЫ",
                "ШТА", "ШТО", "ШТУ", "ШТЭ", "ШТИ", "ШТЫ",
                "ЖДА", "ЖДО", "ЖДУ", "ЖДЭ", "ЖДИ", "ЖДЫ",
                "МНА", "МНО", "МНУ", "МНЭ", "МНИ", "МНЫ"
            )
        ),
        DictionTable(
            id = "lvl3",
            title = "3. Хардкор (42)",
            subtitle = "Ряды: ВЗДРА • ВСТРА • КПТА • СПТРА • МБГДА • ТКПТА • РЛМНА (42 слога)",
            syllables = listOf(
                "ВЗДРА", "ВЗДРО", "ВЗДРУ", "ВЗДРЭ", "ВЗДРИ", "ВЗДРЫ",
                "ВСТРА", "ВСТРО", "ВСТРУ", "ВСТРЭ", "ВСТРИ", "ВСТРЫ",
                "КПТА", "КПТО", "КПТУ", "КПТЭ", "КПТИ", "КПТЫ",
                "СПТРА", "СПТРО", "СПТРУ", "СПТРЭ", "СПТРИ", "СПТРЫ",
                "МБГДА", "МБГДО", "МБГДУ", "МБГДЭ", "МБГДИ", "МБГДЫ",
                "ТКПТА", "ТКПТО", "ТКПТУ", "ТКПТЭ", "ТКПТИ", "ТКПТЫ",
                "РЛМНА", "РЛМНО", "РЛМНУ", "РЛМНЭ", "РЛМНИ", "РЛМНЫ"
            )
        )
    )

    private val VOWELS = listOf("А", "О", "У", "Э", "И", "Ы")
    private val CONSONANT_END_REGEX = Regex("[БВГДЖЗЙКЛМНПРСТФХЦЧШЩ]$")

    fun parseClustersToSyllables(rawInput: String): List<String> {
        val tokens = rawInput
            .split(Regex("[,;\\s\\n]+"))
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
        val result = mutableListOf<String>()
        for (tok in tokens) {
            if (CONSONANT_END_REGEX.containsMatchIn(tok)) {
                for (v in VOWELS) {
                    result.add(tok + v)
                }
            } else {
                result.add(tok)
            }
        }
        return result.take(200)
    }
}
