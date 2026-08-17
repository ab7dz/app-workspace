package com.example.data

import com.example.model.DorkQuery
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.model.SocialPost
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.random.Random

/**
 * Intelligent topic-aware synthetic social listening generator.
 * Analyzes the user's search query, classifies the domain, and synthesizes authentic,
 * varied, dialect-rich social media posts across Facebook, X (Twitter), Instagram, and LinkedIn.
 */
object TopicAwareFeedGenerator {

    enum class TopicDomain {
        SPORTS,
        ECONOMY_FINANCE,
        TECH_AI,
        FOOD_DINING,
        BRANDS_COMMERCE,
        ENTERTAINMENT_MEDIA,
        GENERAL_NEWS
    }

    private fun detectDomain(keyword: String): TopicDomain {
        val lower = keyword.lowercase()
        return when {
            lower.containsAny("مباراة", "كورة", "دوري", "الهلال", "النصر", "الاتحاد", "الأهلي", "ريال", "برشلونة", "ميسي", "رونالدو", "هدف", "كأس", "بطولة", "لاعب", "مدرب", "football", "match", "cup", "league") -> TopicDomain.SPORTS
            lower.containsAny("سهم", "تداول", "ذهب", "دولار", "بتكوين", "كريبتو", "أرباح", "تضخم", "عقار", "استثمار", "بنك", "اقتصاد", "تمويل", "ميزانية", "crypto", "stock", "bitcoin", "finance") -> TopicDomain.ECONOMY_FINANCE
            lower.containsAny("ذكاء", "ai", "تقنية", "برمجة", "تطبيق", "شات", "chatgpt", "gemini", "أبل", "سامسونج", "أندرويد", "سيرفر", "كود", "هاتف", "tech", "software", "ios", "app") -> TopicDomain.TECH_AI
            lower.containsAny("مطعم", "كافيه", "قهوة", "أكل", "شاورما", "برجر", "وجبة", "طعام", "شيف", "حلا", "food", "cafe", "coffee", "restaurant") -> TopicDomain.FOOD_DINING
            lower.containsAny("سيارة", "تسلا", "تويوتا", "شراء", "تخفيض", "سعر", "متجر", "نون", "أمازون", "خدمة", "عملاء", "ضمان", "تجربة", "منتج", "car", "tesla", "deal", "brand") -> TopicDomain.BRANDS_COMMERCE
            lower.containsAny("فيلم", "مسلسل", "سينما", "أغنية", "ممثل", "مسرحية", "حفل", "موسيقى", "نتفلكس", "movie", "series", "music", "actor") -> TopicDomain.ENTERTAINMENT_MEDIA
            else -> TopicDomain.GENERAL_NEWS
        }
    }

    private fun String.containsAny(vararg words: String): Boolean {
        return words.any { this.contains(it) }
    }

    fun generateFeed(query: DorkQuery): List<SocialPost> {
        val kw = if (query.keyword.isNotBlank()) query.keyword.trim() else "الذكاء الاصطناعي"
        val domain = detectDomain(kw)
        val platforms = if (query.selectedPlatforms.isNotEmpty()) query.selectedPlatforms else setOf(PlatformType.FACEBOOK, PlatformType.X_TWITTER)

        val posts = mutableListOf<SocialPost>()

        val domainTemplates = getTemplatesForDomain(domain, kw)

        var idCounter = 1
        for (tpl in domainTemplates) {
            val platform = when {
                tpl.preferredPlatform != null && platforms.contains(tpl.preferredPlatform) -> tpl.preferredPlatform
                platforms.contains(PlatformType.X_TWITTER) && idCounter % 2 == 0 -> PlatformType.X_TWITTER
                platforms.contains(PlatformType.FACEBOOK) -> PlatformType.FACEBOOK
                else -> platforms.firstOrNull() ?: PlatformType.X_TWITTER
            }

            val encodedKw = URLEncoder.encode(kw, StandardCharsets.UTF_8.toString())
            val cleanUrl = when (platform) {
                PlatformType.X_TWITTER -> "https://x.com/search?q=$encodedKw"
                PlatformType.FACEBOOK -> "https://www.facebook.com/search/posts/?q=$encodedKw"
                PlatformType.INSTAGRAM -> "https://www.instagram.com/explore/tags/${kw.replace(" ", "_")}/"
                PlatformType.LINKEDIN -> "https://www.linkedin.com/search/results/content/?keywords=$encodedKw"
                PlatformType.REDDIT -> "https://www.reddit.com/search/?q=$encodedKw"
                PlatformType.YOUTUBE -> "https://www.youtube.com/results?search_query=$encodedKw"
                PlatformType.OTHER -> "https://www.google.com/search?q=$encodedKw"
            }

            posts.add(
                SocialPost(
                    id = "gen_${System.currentTimeMillis()}_$idCounter",
                    platform = platform,
                    title = tpl.title,
                    snippet = tpl.snippet,
                    postUrl = cleanUrl,
                    cleanUrl = cleanUrl,
                    authorName = tpl.authorName,
                    authorHandle = tpl.authorHandle,
                    authorAvatarUrl = tpl.avatarUrl,
                    thumbnailUrl = tpl.imageUrl,
                    publishedTime = tpl.timeAgo,
                    engagementLikes = tpl.likes + Random.nextInt(10, 85),
                    engagementComments = tpl.comments + Random.nextInt(2, 20),
                    engagementShares = tpl.shares + Random.nextInt(1, 15),
                    sentiment = tpl.sentiment,
                    sentimentConfidence = tpl.confidence,
                    sentimentReason = tpl.reason,
                    extractedHashtags = tpl.hashtags,
                    isBookmarked = false
                )
            )
            idCounter++
        }

        return posts.sortedByDescending { it.engagementLikes + it.engagementComments * 2 }
    }

    private data class PostTemplate(
        val title: String,
        val snippet: String,
        val authorName: String,
        val authorHandle: String,
        val avatarUrl: String? = null,
        val imageUrl: String? = null,
        val timeAgo: String,
        val likes: Int,
        val comments: Int,
        val shares: Int,
        val sentiment: SentimentType,
        val confidence: Float,
        val reason: String,
        val hashtags: List<String>,
        val preferredPlatform: PlatformType? = null
    )

    private fun getTemplatesForDomain(domain: TopicDomain, kw: String): List<PostTemplate> {
        val cleanTag = "#" + kw.replace(" ", "_").take(25)
        return when (domain) {
            TopicDomain.SPORTS -> listOf(
                PostTemplate(
                    title = "صدى الملاعب | تحليل فني شامل حول $kw",
                    snippet = "مستوى استثنائي وأداء تكتيكي مبهر متعلق بـ $kw في مواجهة الليلة! التناغم بين الخطوط وصناعة الفرص تعكس عمل تدريبي جبار يستحق الإشادة ⚽🔥",
                    authorName = "صدى الرياضة والملاعب",
                    authorHandle = "@SadaSportHQ",
                    imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 25 دقيقة",
                    likes = 1240,
                    comments = 210,
                    shares = 180,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.96f,
                    reason = "إشادة كبيرة بالأداء الفني والنتيجة المحققة",
                    hashtags = listOf(cleanTag, "#دوري_روشن", "#كورة_عالمية"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "كابتن خالد الشمري on X: \"علامات استفهام حول $kw\"",
                    snippet = "بصراحة، القرارات الأخيرة المرتبطة بـ $kw غير موفقة وتكرار للأخطاء الدفاعية الفادحة. إذا استمر هذا التخبط ستكون العواقب وخيمة على مسيرة الفريق!",
                    authorName = "كابتن خالد الشمري",
                    authorHandle = "@Khaled_Analyst",
                    timeAgo = "منذ ساعة",
                    likes = 870,
                    comments = 195,
                    shares = 84,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.92f,
                    reason = "انتقاد حاد للأخطاء الدفاعية والقرارات الإدارية",
                    hashtags = listOf(cleanTag, "#تحليل_كروي", "#نقاش_رياضي"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "شبكة كووورة لايف | إحصائيات وأرقام $kw",
                    snippet = "رسمياً: الكشف عن إحصائيات الجولة وجدول الترتيب المحدث بعد أحداث $kw. الأرقام تظهر تسجيل 14 هدفاً في 4 مباريات ومعدل استحواذ وصل إلى 58%.",
                    authorName = "كووورة لايف الإخبارية",
                    authorHandle = "@KoooraLiveNews",
                    imageUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 3 ساعات",
                    likes = 560,
                    comments = 45,
                    shares = 32,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.89f,
                    reason = "بيانات وأرقام إحصائية مجردة دون انحياز",
                    hashtags = listOf(cleanTag, "#إحصائيات", "#نتائج"),
                    preferredPlatform = PlatformType.FACEBOOK
                ),
                PostTemplate(
                    title = "منتدى عشاق الجماهير | تفاعل جنوني مع $kw",
                    snippet = "أجواء خيالية في المدرجات وتيفو للتاريخ احتفالاً بـ $kw! الجمهور هو الرقم الصعب والروح الحقيقية لأي انتصار 👏💚",
                    authorName = "رابطة المشجعين الأوفياء",
                    authorHandle = "@FansVoice_Arabia",
                    timeAgo = "منذ 4 ساعات",
                    likes = 2150,
                    comments = 320,
                    shares = 410,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.95f,
                    reason = "احتفال جماهيري وحماس إيجابي مرتفع",
                    hashtags = listOf(cleanTag, "#جمهور_الذهب", "#مدرجات"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.ECONOMY_FINANCE -> listOf(
                PostTemplate(
                    title = "المرصد الاقتصادي | تقرير حركة الأسواق حول $kw",
                    snippet = "تسجيل قفزة إيجابية ومؤشرات نمو قوية في قطاع $kw بنسبة تجاوزت 4.2% مدعومة بسيولة استثمارية ضخمة وثقة متزايدة من الصناديق الاستثمارية 📈🇸🇦",
                    authorName = "المرصد المالي والاقتصادي",
                    authorHandle = "@EconomyWatchArabia",
                    imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 30 دقيقة",
                    likes = 940,
                    comments = 112,
                    shares = 145,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.94f,
                    reason = "مؤشرات نمو مالي وتدفقات سيولة استثمارية قوية",
                    hashtags = listOf(cleanTag, "#اقتصاد", "#أسواق_المال", "#تداول"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "م. فيصل التميمي (محلل أسواق) | تحذير من تقلبات $kw",
                    snippet = "انتبهوا من المضاربات العشوائية الحالية في $kw. هناك مؤشرات تشبع شرائي وتصحيح سعري قادم قد يكبد المتداولين خسائر سريعة. إدارة المخاطر أولاً ⚠️",
                    authorName = "م. فيصل التميمي",
                    authorHandle = "@Faisal_Trading",
                    timeAgo = "منذ ساعتين",
                    likes = 680,
                    comments = 140,
                    shares = 95,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.91f,
                    reason = "تحذير من مخاطر الخسائر وتصحيح هبوطي محتمل",
                    hashtags = listOf(cleanTag, "#تحليل_فني", "#إدارة_المخاطر"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "نشرة الأعمال اليومية | إعلان رسمي يخص $kw",
                    snippet = "أعلنت الجهات التنظيمية اليوم عن اعتماد اللائحة التنفيذية المنظمة لـ $kw والتي تهدف لتعزيز الشفافية وتوحيد معايير الإفصاح المالي لجميع المتعاملين.",
                    authorName = "نشرة الأعمال والاستثمار",
                    authorHandle = "@BusinessDigestME",
                    timeAgo = "منذ 4 ساعات",
                    likes = 430,
                    comments = 28,
                    shares = 60,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.90f,
                    reason = "بيان تنظيمي وتشريعي رسمي بدون آراء",
                    hashtags = listOf(cleanTag, "#أخبار_الأعمال", "#تنظيم_الأسواق"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.TECH_AI -> listOf(
                PostTemplate(
                    title = "مجتمع المطورين والذكاء الاصطناعي | ثورة جديدة في $kw",
                    snippet = "تجربتنا التقييمية لـ $kw أثبتت كفاءة غير مسبوقة في تسريع تدفق العمليات وتوفير أكثر من 60% من وقت البناء والبرمجة! إنجاز تقني واعد جداً 🚀🤖",
                    authorName = "عالم التقنية والابتكار",
                    authorHandle = "@TechWorldArabia",
                    imageUrl = "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 15 دقيقة",
                    likes = 1890,
                    comments = 240,
                    shares = 310,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.97f,
                    reason = "إشادة كبيرة بالكفاءة وتوفير الوقت والأداء",
                    hashtags = listOf(cleanTag, "#تقنية", "#ذكاء_اصطناعي", "#ابتكار"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "د. عمر الأنصاري (أمن سيبراني) | ثغرات وتحديات $kw",
                    snippet = "رغم الوعود الكبيرة، إلا أن الاعتماد الأعمى على $kw يثير مخاوف جسيمة تتعلق بخصوصية البيانات الحساسة وتسريب التوكنات الأمنية. الحذر واجب!",
                    authorName = "د. عمر الأنصاري",
                    authorHandle = "@Omar_Security",
                    timeAgo = "منذ ساعة ونصف",
                    likes = 750,
                    comments = 89,
                    shares = 130,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.93f,
                    reason = "مخاوف أمنية وتحذير من تسريب البيانات والخصوصية",
                    hashtags = listOf(cleanTag, "#أمن_المعلومات", "#حماية_البيانات"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "منصة كلاود تك | مقارنة تفصيلية لـ $kw",
                    snippet = "جدول مقارنة كامل بين $kw والبدائل المتاحة في السوق: استهلاك الذاكرة، سرعة الاستجابة بالمللي ثانية، وتكلفة التراخيص السنوية لكل مستخدم.",
                    authorName = "منصة كلاود تك",
                    authorHandle = "@CloudTechGuide",
                    imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 5 ساعات",
                    likes = 520,
                    comments = 34,
                    shares = 78,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.88f,
                    reason = "مقارنة معيارية فنية ومواصفات تقنية",
                    hashtags = listOf(cleanTag, "#برمجة", "#بنية_تحتية"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.FOOD_DINING -> listOf(
                PostTemplate(
                    title = "تجارب الذواقة | مراجعة خاصة لـ $kw",
                    snippet = "تجربة فاخرة ولذيذة جداً اليوم مع $kw! جودة المكونات، حسن الاستقبال، وسرعة الخدمة تستحق 10/10 بجدارة. أنصح بتجربته وبشدة 👌🍽️",
                    authorName = "دليل الذواقة والمطاعم",
                    authorHandle = "@FoodieSaudi",
                    imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 40 دقيقة",
                    likes = 1420,
                    comments = 185,
                    shares = 92,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.96f,
                    reason = "تقييم عالي للمذاق وجودة الخدمة والتجربة",
                    hashtags = listOf(cleanTag, "#أماكن_الرياض", "#مطاعم_جدة", "#تجارب"),
                    preferredPlatform = PlatformType.FACEBOOK
                ),
                PostTemplate(
                    title = "سلطان العتيبي on X: \"تجربة محبطة مع $kw\"",
                    snippet = "للأسف تجربة مخيبة للآمال مع $kw. انتظار أكثر من 45 دقيقة، والأسعار مبالغ فيها جداً مقارنة بالكمية وجودة التقديم! لن أكرر الزيارة.",
                    authorName = "سلطان العتيبي",
                    authorHandle = "@Sultan_Otaibi",
                    timeAgo = "منذ ساعتين",
                    likes = 620,
                    comments = 148,
                    shares = 45,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.94f,
                    reason = "شكوى من بطء الخدمة والأسعار المبالغ فيها",
                    hashtags = listOf(cleanTag, "#خدمة_العملاء", "#تقييم_صريح"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "دليل المقاهي والوجهات | تفاصيل افتتاح $kw",
                    snippet = "مواعيد العمل الرسمية، قائمة الأصناف، والأسعار المعتمدة لـ $kw في فروعه الجديدة من الساعة 7 صباحاً وحتى 12 منتصف الليل.",
                    authorName = "دليل الوجهات الترفيهية",
                    authorHandle = "@CityGuide_KSA",
                    timeAgo = "منذ 6 ساعات",
                    likes = 380,
                    comments = 22,
                    shares = 35,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.90f,
                    reason = "معلومات تشغيلية ومواعيد بدون تقييم شخصي",
                    hashtags = listOf(cleanTag, "#دليل_الوجهات", "#افتتاح"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.BRANDS_COMMERCE -> listOf(
                PostTemplate(
                    title = "رأي المستهلك الذكي | تقييم منتج $kw",
                    snippet = "بعد تجربة استمرت أسبوعين لـ $kw: خامات ممتازة، بطارية تصمد طويلاً، وسعر منافس جداً في فئته. تجربة شراء ناجحة وأنصح بها 🌟📦",
                    authorName = "المستهلك الذكي",
                    authorHandle = "@SmartConsumer_Ar",
                    imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 50 دقيقة",
                    likes = 1130,
                    comments = 130,
                    shares = 77,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.95f,
                    reason = "رضا كامل عن جودة الخامات والتسعير المنافس",
                    hashtags = listOf(cleanTag, "#تسوق", "#مراجعات_منتجات"),
                    preferredPlatform = PlatformType.FACEBOOK
                ),
                PostTemplate(
                    title = "عبدالله الحربي on X: \"تأخير واستياء من خدمة $kw\"",
                    snippet = "أسوأ تجربة دعم فني مع $kw. الطلب متأخر 10 أيام ولا يوجد أي رد من خدمة العملاء ولا تتبع للشحنة! أين حماية المستهلك؟ 😡",
                    authorName = "عبدالله الحربي",
                    authorHandle = "@Abdullah_AlHarbi",
                    timeAgo = "منذ ساعتين",
                    likes = 890,
                    comments = 210,
                    shares = 115,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.97f,
                    reason = "غضب عارم بسبب تأخر الشحنة وسوء خدمة العملاء",
                    hashtags = listOf(cleanTag, "#خدمة_العملاء", "#حماية_المستهلك"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "أخبار التجارة والصناعة | إطلاق حملة $kw",
                    snippet = "أطلقت الشركة رسمياً مواصفات وفئات $kw بالسوق المحلي مع توفير ضمان لمدة سنتين وخدمة التوصيل السريع لجميع المناطق.",
                    authorName = "أخبار التجارة والصناعة",
                    authorHandle = "@CommerceNews_ME",
                    timeAgo = "منذ 7 ساعات",
                    likes = 410,
                    comments = 19,
                    shares = 42,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.91f,
                    reason = "إعلان تسويقي رسمي ومواصفات تجارية",
                    hashtags = listOf(cleanTag, "#تجارة", "#منتجات_جديدة"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.ENTERTAINMENT_MEDIA -> listOf(
                PostTemplate(
                    title = "سينما ومسلسلات | مراجعة نقدية لـ $kw",
                    snippet = "إبداع سينمائي متكامل في $kw! الحبكة مشوقة، التصوير والموسيقى التصويرية تحبس الأنفاس، وأداء الممثلين يستحق كل الجوائز 🎬✨",
                    authorName = "نادي السينما والنقاد",
                    authorHandle = "@CinemaClub_Arab",
                    imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ نصف ساعة",
                    likes = 1750,
                    comments = 290,
                    shares = 210,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.96f,
                    reason = "إشادة كبيرة بالحبكة والإخراج والموسيقى",
                    hashtags = listOf(cleanTag, "#سينما", "#مسلسلات", "#دراما"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "ناقد فني مستقل on X: \"سقطة كبيرة في $kw\"",
                    snippet = "للأسف الشديد خيبة أمل بعد الضجة الدعائية الكبيرة لـ $kw. سيناريو ركيك ونهاية مبتذلة لا تليق بالأسماء المشاركة!",
                    authorName = "أحمد السعدون",
                    authorHandle = "@Ahmed_Critic",
                    timeAgo = "منذ 3 ساعات",
                    likes = 780,
                    comments = 160,
                    shares = 68,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.93f,
                    reason = "انتقاد للسيناريو وضعف الحبكة والنهاية",
                    hashtags = listOf(cleanTag, "#نقد_فني", "#رأي_صريح"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "دليل المشاهدة | تفاصيل عرض $kw",
                    snippet = "جدول مواعيد بث $kw على المنصات الرقمية وشاشات التلفاز مع تفاصيل عدد الحلقات وتصنيف الفئة العمرية (+16).",
                    authorName = "دليل الشاشة والترفيه",
                    authorHandle = "@ScreenGuide_Ar",
                    timeAgo = "منذ 8 ساعات",
                    likes = 490,
                    comments = 25,
                    shares = 50,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.89f,
                    reason = "معلومات برامج وتوقيتات عرض مجردة",
                    hashtags = listOf(cleanTag, "#ترفيه", "#دليل_المشاهدة"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )

            TopicDomain.GENERAL_NEWS -> listOf(
                PostTemplate(
                    title = "منتدى الحوار والنقاش العام | أصداء واسعة حول $kw",
                    snippet = "تفاعل مجتمعي ملحوظ وتأييد واسع لخطوات $kw التي تساهم في تحسين جودة الحياة وفتح آفاق جديدة للشباب والمجتمع 👏✨",
                    authorName = "نبض الشارع والمجتمع",
                    authorHandle = "@SocialPulse_Ar",
                    imageUrl = "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=500&auto=format&fit=crop&q=60",
                    timeAgo = "منذ 20 دقيقة",
                    likes = 1320,
                    comments = 180,
                    shares = 140,
                    sentiment = SentimentType.POSITIVE,
                    confidence = 0.94f,
                    reason = "تأييد مجتمعي واسع وانطباع إيجابي عام",
                    hashtags = listOf(cleanTag, "#المجتمع", "#تطور", "#جودة_الحياة"),
                    preferredPlatform = PlatformType.FACEBOOK
                ),
                PostTemplate(
                    title = "رأي مواطن on X: \"تساؤلات ملحة حول $kw\"",
                    snippet = "نحتاج إلى توضيحات رسمية أكثر شفافية بخصوص $kw لمعالجة الصعوبات الميدانية التي تواجه المستفيدين وتفادي التعطيل الحالي.",
                    authorName = "م. ناصر الغامدي",
                    authorHandle = "@Nasser_AlGhamdi",
                    timeAgo = "منذ ساعتين",
                    likes = 670,
                    comments = 135,
                    shares = 88,
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.90f,
                    reason = "مطالبات بتوضيحات رسمية بسبب صعوبات ميدانية",
                    hashtags = listOf(cleanTag, "#صوت_المواطن", "#نقاش_عام"),
                    preferredPlatform = PlatformType.X_TWITTER
                ),
                PostTemplate(
                    title = "وكالة الأنباء الإخبارية | بيان رسمي يخص $kw",
                    snippet = "أعلنت الجهات المعنية اليوم عن إطلاق المبادرة الشاملة المتعلقة بـ $kw بالتعاون مع القطاعين العام والخاص وفق خطة زمنية محددة.",
                    authorName = "مرصد الأخبار الرسمية",
                    authorHandle = "@NewsObserver_Ar",
                    timeAgo = "منذ 5 ساعات",
                    likes = 510,
                    comments = 30,
                    shares = 45,
                    sentiment = SentimentType.NEUTRAL,
                    confidence = 0.92f,
                    reason = "تغطية إخبارية رسمية محايدة للمبادرة",
                    hashtags = listOf(cleanTag, "#أخبار", "#مبادرات_وطنية"),
                    preferredPlatform = PlatformType.FACEBOOK
                )
            )
        }
    }
}
