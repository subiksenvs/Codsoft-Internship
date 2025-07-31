package com.example.quoteoftheday

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for quotes
data class Quote(
    val text: String,
    val author: String,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

// Expanded list of quotes
val sampleQuotes = listOf(
    Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
    Quote("Innovation distinguishes between a leader and a follower.", "Steve Jobs"),
    Quote("Your time is limited, don't waste it living someone else's life.", "Steve Jobs"),
    Quote("Stay hungry, stay foolish.", "Steve Jobs"),
    Quote("The greatest glory in living lies not in never falling, but in rising every time we fall.", "Nelson Mandela"),
    Quote("The way to get started is to quit talking and begin doing.", "Walt Disney"),
    Quote("If life were predictable it would cease to be life, and be without flavor.", "Eleanor Roosevelt"),
    Quote("Life is what happens when you're busy making other plans.", "John Lennon"),
    Quote("Spread love everywhere you go. Let no one ever come to you without leaving happier.", "Mother Teresa"),
    Quote("When you reach the end of your rope, tie a knot in it and hang on.", "Franklin D. Roosevelt"),
    Quote("Don't judge each day by the harvest you reap but by the seeds that you plant.", "Robert Louis Stevenson"),
    Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
    Quote("Tell me and I forget. Teach me and I remember. Involve me and I learn.", "Benjamin Franklin"),
    Quote("The best and most beautiful things in the world cannot be seen or even touched — they must be felt with the heart.", "Helen Keller"),
    Quote("It is during our darkest moments that we must focus to see the light.", "Aristotle"),
    Quote("Whoever is happy will make others happy too.", "Anne Frank"),
    Quote("Do not go where the path may lead, go instead where there is no path and leave a trail.", "Ralph Waldo Emerson"),
    Quote("You will face many defeats in life, but never let yourself be defeated.", "Maya Angelou"),
    Quote("The purpose of our lives is to be happy.", "Dalai Lama"),
    Quote("Life is really simple, but we insist on making it complicated.", "Confucius"),
    Quote("In the end, it's not the years in your life that count. It's the life in your years.", "Abraham Lincoln"),
    Quote("Many of life's failures are people who did not realize how close they were to success when they gave up.", "Thomas A. Edison"),
    Quote("You have brains in your head. You have feet in your shoes. You can steer yourself any direction you choose.", "Dr. Seuss"),
    Quote("Life is either a daring adventure or nothing at all.", "Helen Keller"),
    Quote("The only impossible journey is the one you never begin.", "Tony Robbins"),
    Quote("Only a life lived for others is a life worthwhile.", "Albert Einstein"),
    Quote("You must be the change you wish to see in the world.", "Mahatma Gandhi"),
    Quote("What you get by achieving your goals is not as important as what you become by achieving your goals.", "Zig Ziglar"),
    Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
    Quote("I can't change the direction of the wind, but I can adjust my sails to always reach my destination.", "Jimmy Dean"),
    Quote("To live is the rarest thing in the world. Most people exist, that is all.", "Oscar Wilde"),
    Quote("The two most important days in your life are the day you are born and the day you find out why.", "Mark Twain"),
    Quote("Life is 10% what happens to us and 90% how we react to it.", "Charles R. Swindoll"),
    Quote("Too many of us are not living our dreams because we are living our fears.", "Les Brown"),
    Quote("Strive not to be a success, but rather to be of value.", "Albert Einstein"),
    Quote("I attribute my success to this: I never gave or took any excuse.", "Florence Nightingale"),
    Quote("You miss 100% of the shots you don't take.", "Wayne Gretzky"),
    Quote("The most difficult thing is the decision to act, the rest is merely tenacity.", "Amelia Earhart"),
    Quote("Every strike brings me closer to the next home run.", "Babe Ruth"),
    Quote("Definiteness of purpose is the starting point of all achievement.", "W. Clement Stone"),
    Quote("Life isn't about getting and having, it's about giving and being.", "Kevin Kruse"),
    Quote("We become what we think about.", "Earl Nightingale"),
    Quote("Twenty years from now you will be more disappointed by the things that you didn't do than by the ones you did do.", "Mark Twain"),
    Quote("The mind is everything. What you think you become.", "Buddha"),
    Quote("The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
    Quote("An unexamined life is not worth living.", "Socrates"),
    Quote("Eighty percent of success is showing up.", "Woody Allen"),
    Quote("Winning isn't everything, but wanting to win is.", "Vince Lombardi"),
    Quote("I am not a product of my circumstances. I am a product of my decisions.", "Stephen Covey"),
    Quote("Every child is an artist. The problem is how to remain an artist once he grows up.", "Pablo Picasso"),
    Quote("You can never cross the ocean until you have the courage to lose sight of the shore.", "Christopher Columbus"),
    Quote("I've learned that people will forget what you said, people will forget what you did, but people will never forget how you made them feel.", "Maya Angelou"),
    Quote("Either you run the day, or the day runs you.", "Jim Rohn"),
    Quote("Whether you think you can or you think you can't, you're right.", "Henry Ford"),
    Quote("Whatever you can do, or dream you can, begin it. Boldness has genius, power and magic in it.", "Johann Wolfgang von Goethe"),
    Quote("The best revenge is massive success.", "Frank Sinatra"),
    Quote("People often say that motivation doesn't last. Well, neither does bathing—that's why we recommend it daily.", "Zig Ziglar"),
    Quote("Life shrinks or expands in proportion to one's courage.", "Anais Nin"),
    Quote("If you hear a voice within you say 'you cannot paint,' then by all means paint and that voice will be silenced.", "Vincent Van Gogh"),
    Quote("There is only one way to avoid criticism: do nothing, say nothing, and be nothing.", "Aristotle"),
    Quote("The only person you are destined to become is the person you decide to be.", "Ralph Waldo Emerson"),
    Quote("Go confidently in the direction of your dreams. Live the life you have imagined.", "Henry David Thoreau"),
    Quote("When I stand before God at the end of my life, I would hope that I would not have a single bit of talent left and could say, I used everything you gave me.", "Erma Bombeck"),
    Quote("Few things can help an individual more than to place responsibility on him, and to let him know that you trust him.", "Booker T. Washington"),
    Quote("Certain things catch your eye, but pursue only those that capture the heart.", "Ancient Indian Proverb"),
    Quote("Everything you've ever wanted is on the other side of fear.", "George Addair"),
    Quote("We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.", "Plato"),
    Quote("Teach thy tongue to say, 'I do not know,' and thou shalt progress.", "Maimonides"),
    Quote("Start where you are. Use what you have. Do what you can.", "Arthur Ashe"),
    Quote("When I was 5 years old, my mother always told me that happiness was the key to life. When I went to school, they asked me what I wanted to be when I grew up. I wrote down 'happy'. They told me I didn't understand the assignment, and I told them they didn't understand life.", "John Lennon"),
    Quote("Fall seven times and stand up eight.", "Japanese Proverb"),
    Quote("When one door of happiness closes, another opens, but often we look so long at the closed door that we do not see the one that has been opened for us.", "Helen Keller"),
    Quote("Everything has beauty, but not everyone can see.", "Confucius"),
    Quote("How wonderful it is that nobody need wait a single moment before starting to improve the world.", "Anne Frank"),
    Quote("When I let go of what I am, I become what I might be.", "Lao Tzu"),
    Quote("Life is not measured by the number of breaths we take, but by the moments that take our breath away.", "Maya Angelou"),
    Quote("Happiness is not something readymade. It comes from your own actions.", "Dalai Lama"),
    Quote("If you're offered a seat on a rocket ship, don't ask what seat! Just get on.", "Sheryl Sandberg"),
    Quote("First, have a definite, clear practical ideal; a goal, an objective. Second, have the necessary means to achieve your ends; wisdom, money, materials, and methods. Third, adjust all your means to that end.", "Aristotle"),
    Quote("If the wind will not serve, take to the oars.", "Latin Proverb"),
    Quote("You can't fall if you don't climb. But there's no joy in living your whole life on the ground.", "Unknown"),
    Quote("We must believe that we are gifted for something, and that this thing, at whatever cost, must be attained.", "Marie Curie"),
    Quote("Challenges are what make life interesting and overcoming them is what makes life meaningful.", "Joshua J. Marine"),
    Quote("If you want to lift yourself up, lift up someone else.", "Booker T. Washington"),
    Quote("I have been impressed with the urgency of doing. Knowing is not enough; we must apply. Being willing is not enough; we must do.", "Leonardo da Vinci"),
    Quote("Limitations live only in our minds. But if we use our imaginations, our possibilities become limitless.", "Jamie Paolinetti"),
    Quote("You take your life in your own hands, and what happens? A terrible thing, no one to blame.", "Erica Jong"),
    Quote("What's money? A man is a success if he gets up in the morning and goes to bed at night and in between does what he wants to do.", "Bob Dylan"),
    Quote("I didn't fail the test. I just found 100 ways to do it wrong.", "Benjamin Franklin"),
    Quote("In order to succeed, your desire for success should be greater than your fear of failure.", "Bill Cosby"),
    Quote("A person who never made a mistake never tried anything new.", "Albert Einstein"),
    Quote("The person who says it cannot be done should not interrupt the person who is doing it.", "Chinese Proverb"),
    Quote("There are no traffic jams along the extra mile.", "Roger Staubach"),
    Quote("It is never too late to be what you might have been.", "George Eliot"),
    Quote("You become what you believe.", "Oprah Winfrey"),
    Quote("I would rather die of passion than of boredom.", "Vincent van Gogh"),
    Quote("A truly rich man is one whose children run into his arms when his hands are empty.", "Unknown"),
    Quote("It is not what you do for your children, but what you have taught them to do for themselves, that will make them successful human beings.", "Ann Landers"),
    Quote("If you want your children to turn out well, spend twice as much time with them, and half as much money.", "Abigail Van Buren"),
    Quote("Build your own dreams, or someone else will hire you to build theirs.", "Farrah Gray"),
    Quote("The battles that count aren't the ones for gold medals. The struggles within yourself—the invisible battles inside all of us—that's where it's at.", "Jesse Owens"),
    Quote("Education costs money. But then so does ignorance.", "Sir Claus Moser"),
    Quote("I have learned over the years that when one's mind is made up, this diminishes fear.", "Rosa Parks"),
    Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
    Quote("If you look at what you have in life, you'll always have more. If you look at what you don't have in life, you'll never have enough.", "Oprah Winfrey"),
    Quote("Remember that not getting what you want is sometimes a wonderful stroke of luck.", "Dalai Lama"),
    Quote("You can't use up creativity. The more you use, the more you have.", "Maya Angelou"),
    Quote("Dream big and dare to fail.", "Norman Vaughan"),
    Quote("Our lives begin to end the day we become silent about things that matter.", "Martin Luther King Jr."),
    Quote("Do what you can, where you are, with what you have.", "Teddy Roosevelt"),
    Quote("If you do what you've always done, you'll get what you've always gotten.", "Tony Robbins"),
    Quote("Dreaming, after all, is a form of planning.", "Gloria Steinem"),
    Quote("It's your place in the world; it's your life. Go on and do all you can with it, and make it the life you want to live.", "Mae Jemison"),
    Quote("You may be disappointed if you fail, but you are doomed if you don't try.", "Beverly Sills"),
    Quote("Remember no one can make you feel inferior without your consent.", "Eleanor Roosevelt"),
    Quote("Life is what we make it, always has been, always will be.", "Grandma Moses"),
    Quote("The question isn't who is going to let me; it's who is going to stop me.", "Ayn Rand"),
    Quote("When everything seems to be going against you, remember that the airplane takes off against the wind, not with it.", "Henry Ford"),
    Quote("Change your thoughts and you change your world.", "Norman Vincent Peale"),
    Quote("Either write something worth reading or do something worth writing.", "Benjamin Franklin"),
    Quote("Nothing is impossible, the word itself says, 'I'm possible!'", "Audrey Hepburn"),
    Quote("If you can dream it, you can achieve it.", "Zig Ziglar"),
    Quote("The best way to predict the future is to invent it.", "Alan Kay"),
    Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
    Quote("A creative man is motivated by the desire to achieve, not by the desire to beat others.", "Ayn Rand"),
    Quote("The only limit to our realization of tomorrow will be our doubts of today.", "Franklin D. Roosevelt"),
    Quote("What you do today can improve all your tomorrows.", "Ralph Marston"),
    Quote("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
    Quote("Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau"),
    Quote("Don't be afraid to give up the good to go for the great.", "John D. Rockefeller"),
    Quote("I find that the harder I work, the more luck I seem to have.", "Thomas Jefferson"),
    Quote("The secret of getting ahead is getting started.", "Mark Twain"),
    Quote("Opportunities don't happen. You create them.", "Chris Grosser"),
    Quote("The only place where success comes before work is in the dictionary.", "Vidal Sassoon"),
    Quote("Don't let yesterday take up too much of today.", "Will Rogers"),
    Quote("The successful warrior is the average man, with laser-like focus.", "Bruce Lee"),
    Quote("There are no shortcuts to any place worth going.", "Beverly Sills"),
    Quote("I never dreamed about success. I worked for it.", "Estée Lauder"),
    Quote("Success is walking from failure to failure with no loss of enthusiasm.", "Winston Churchill"),
    Quote("The road to success and the road to failure are almost exactly the same.", "Colin R. Davis"),
    Quote("Success is liking yourself, liking what you do, and liking how you do it.", "Maya Angelou"),
    Quote("The secret of success is to do the common thing uncommonly well.", "John D. Rockefeller Jr."),
    Quote("I owe my success to having listened respectfully to the very best advice, and then going away and doing the exact opposite.", "G.K. Chesterton"),
    Quote("Success seems to be connected with action. Successful people keep moving. They make mistakes, but they don't quit.", "Conrad Hilton"),
    Quote("All progress takes place outside the comfort zone.", "Michael John Bobak"),
    Quote("The only thing standing between you and your goal is the bullshit story you keep telling yourself as to why you can't achieve it.", "Jordan Belfort"),
    Quote("Success is not in what you have, but who you are.", "Bo Bennett"),
    Quote("The successful man is the one who finds out what is the matter with his business before his competitors do.", "Roy L. Smith"),
    Quote("I have not failed. I've just found 10,000 ways that won't work.", "Thomas A. Edison"),
    Quote("The ones who are crazy enough to think they can change the world, are the ones who do.", "Steve Jobs"),
    Quote("Don't raise your voice, improve your argument.", "Desmond Tutu"),
    Quote("What seems to us as bitter trials are often blessings in disguise.", "Oscar Wilde"),
    Quote("The meaning of life is to find your gift. The purpose of life is to give it away.", "Pablo Picasso"),
    Quote("The distance between insanity and genius is measured only by success.", "Bruce Feirstein"),
    Quote("When you stop chasing the wrong things, you give the right things a chance to catch you.", "Lolly Daskal"),
    Quote("I believe that the only courage anybody ever needs is the courage to follow your own dreams.", "Oprah Winfrey"),
    Quote("No masterpiece was ever created by a lazy artist.", "Unknown"),
    Quote("Happiness is a butterfly, which when pursued, is always just beyond your grasp, but which, if you will sit down quietly, may alight upon you.", "Nathaniel Hawthorne"),
    Quote("If you can't explain it simply, you don't understand it well enough.", "Albert Einstein"),
    Quote("Blessed are those who can give without remembering and take without forgetting.", "Elizabeth Bibesco"),
    Quote("Do one thing every day that scares you.", "Eleanor Roosevelt"),
    Quote("What's the point of being alive if you don't at least try to do something remarkable?", "John Green"),
    Quote("Life is not about finding yourself. Life is about creating yourself.", "George Bernard Shaw"),
    Quote("Nothing in the world is more common than unsuccessful people with talent.", "Unknown"),
    Quote("Knowledge is being aware of what you can do. Wisdom is knowing when not to do it.", "Unknown"),
    Quote("Your problem isn't the problem. Your reaction is the problem.", "Unknown"),
    Quote("You can do anything, but not everything.", "Unknown"),
    Quote("There are two types of people who will tell you that you cannot make a difference in this world: those who are afraid to try and those who are afraid you will succeed.", "Ray Goforth"),
    Quote("Thinking should become your capital asset, no matter whatever ups and downs you come across in your life.", "A.P.J. Abdul Kalam"),
    Quote("I find that when you have a real interest in life and a curious life, that sleep is not the most important thing.", "Martha Stewart"),
    Quote("It's not what you look at that matters, it's what you see.", "Henry David Thoreau"),
    Quote("The entrepreneur is essentially a visualizer and actualizer... He can visualize something, and when he visualizes it he sees exactly how to make it happen.", "Robert L. Schwartz"),
    Quote("The function of leadership is to produce more leaders, not more followers.", "Ralph Nader"),
    Quote("A successful man is one who can lay a firm foundation with the bricks others have thrown at him.", "David Brinkley"),
    Quote("The whole secret of a successful life is to find out what is one's destiny to do, and then do it.", "Henry Ford"),
    Quote("If you're going through hell, keep going.", "Winston Churchill"),
    Quote("Don't let the fear of losing be greater than the excitement of winning.", "Robert Kiyosaki"),
    Quote("Our greatest weakness lies in giving up. The most certain way to succeed is always to try just one more time.", "Thomas A. Edison"),
    Quote("The real test is not whether you avoid this failure, because you won't. It's whether you let it harden or shame you into inaction, or whether you learn from it; whether you choose to persevere.", "Barack Obama"),
    Quote("It is hard to fail, but it is worse never to have tried to succeed.", "Theodore Roosevelt"),
    Quote("If you don't design your own life plan, chances are you'll fall into someone else's plan. And guess what they have planned for you? Not much.", "Jim Rohn"),
    Quote("The starting point of all achievement is desire.", "Napoleon Hill"),
    Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
    Quote("If you want to achieve excellence, you can get there today. As of this second, quit doing less-than-excellent work.", "Thomas J. Watson"),
    Quote("You may only succeed if you desire succeeding; you may only fail if you do not mind failing.", "Philippos"),
    Quote("Courage is resistance to fear, mastery of fear—not absence of fear.", "Mark Twain"),
    Quote("Only put off until tomorrow what you are willing to die having left undone.", "Pablo Picasso"),
    Quote("We become what we think about most of the time, and that's the strangest secret.", "Earl Nightingale"),
    Quote("As we look ahead into the next century, leaders will be those who empower others.", "Bill Gates"),
    Quote("A real entrepreneur is somebody who has no safety net underneath them.", "Henry Kravis"),
    Quote("The first step toward success is taken when you refuse to be a captive of the environment in which you first find yourself.", "Mark Caine"),
    Quote("When I dare to be powerful—to use my strength in the service of my vision—then it becomes less and less important whether I am afraid.", "Audre Lorde"),
    Quote("Whenever you find yourself on the side of the majority, it is time to pause and reflect.", "Mark Twain"),
    Quote("Develop success from failures. Discouragement and failure are two of the surest stepping stones to success.", "Dale Carnegie"),
    Quote("If you genuinely want something, don't wait for it—teach yourself to be impatient.", "Gurbaksh Chahal"),
    Quote("If you want to make a permanent change, stop focusing on the size of your problems and start focusing on the size of you!", "T. Harv Eker"),
    Quote("The harder you work for something, the greater you'll feel when you achieve it.", "Unknown"),
    Quote("Dream bigger. Do bigger.", "Unknown"),
    Quote("Don't stop when you're tired. Stop when you're done.", "Unknown"),
    Quote("Wake up with determination. Go to bed with satisfaction.", "Unknown"),
    Quote("Do something today that your future self will thank you for.", "Unknown"),
    Quote("Little things make big days.", "Unknown"),
    Quote("It's going to be hard, but hard does not mean impossible.", "Unknown"),
    Quote("Don't wait for opportunity. Create it.", "Unknown"),
    Quote("Sometimes we're tested not to show our weaknesses, but to discover our strengths.", "Unknown"),
    Quote("The key to success is to focus on goals, not obstacles.", "Unknown"),
    Quote("Dream it. Believe it. Build it.", "Unknown"),
    Quote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuoteAppTheme {
                QuoteApp()
            }
        }
    }
}

@Composable
fun QuoteAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuoteApp() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentQuotes by remember { mutableStateOf(List(3) { getRandomQuote() }) }
    var favoriteQuotes by remember { mutableStateOf(emptyList<Quote>()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today's Quotes", "Favorites")
    val pagerState = rememberPagerState(pageCount = { currentQuotes.size })

    LaunchedEffect(Unit) {
        try {
            favoriteQuotes = loadFavorites(context)
        } catch (e: Exception) {
            Log.e("QuoteApp", "Error loading favorites", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading favorites")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Inspirational Quotes",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = Color.Transparent
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> TodayQuotesScreen(
                    quotes = currentQuotes,
                    pagerState = pagerState,
                    favoriteQuotes = favoriteQuotes,
                    onRefresh = {
                        currentQuotes = List(3) { getRandomQuote() }
                        coroutineScope.launch { pagerState.scrollToPage(0) }
                    },
                    onShare = { shareQuote(context, currentQuotes[pagerState.currentPage]) },
                    onToggleFavorite = { quote ->
                        coroutineScope.launch {
                            favoriteQuotes = toggleFavorite(context, favoriteQuotes, quote)
                            val message = if (favoriteQuotes.any { it.text == quote.text }) {
                                "Added to favorites"
                            } else {
                                "Removed from favorites"
                            }
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
                1 -> FavoriteQuotesScreen(
                    quotes = favoriteQuotes,
                    onShare = { shareQuote(context, it) },
                    onRemove = { quote ->
                        coroutineScope.launch {
                            favoriteQuotes = removeFavorite(context, favoriteQuotes, quote)
                            snackbarHostState.showSnackbar("Removed from favorites")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayQuotesScreen(
    quotes: List<Quote>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    favoriteQuotes: List<Quote>,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: (Quote) -> Unit
) {
    val gradientColors = listOf(
        Color(0xFF6A11CB),
        Color(0xFF2575FC),
        Color(0xFF1488CC),
        Color(0xFF4776E6)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val quote = quotes[page]
                val isFavorite = favoriteQuotes.any { it.text == quote.text }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❝${quote.text}❞",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "— ${quote.author}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = quote.date,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Pager indicators
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(quotes.size) { index ->
                    val color = if (pagerState.currentPage == index)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh quotes",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(quotes[pagerState.currentPage]) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = if (favoriteQuotes.any { it.text == quotes[pagerState.currentPage].text })
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (favoriteQuotes.any { it.text == quotes[pagerState.currentPage].text })
                            "Remove from favorites"
                        else
                            "Add to favorites",
                        tint = if (favoriteQuotes.any { it.text == quotes[pagerState.currentPage].text })
                            Color.Red
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share quote",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteQuotesScreen(
    quotes: List<Quote>,
    onShare: (Quote) -> Unit,
    onRemove: (Quote) -> Unit
) {
    if (quotes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "No favorites",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No favorite quotes yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Swipe through quotes and tap the heart to add favorites",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quotes) { quote ->
                FavoriteQuoteItem(
                    quote = quote,
                    onShare = { onShare(quote) },
                    onRemove = { onRemove(quote) }
                )
            }
        }
    }
}

@Composable
fun FavoriteQuoteItem(
    quote: Quote,
    onShare: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShare() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "❝${quote.text}❞",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = quote.date,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove from favorites",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Helper functions
fun getRandomQuote(): Quote {
    return sampleQuotes.random()
}

fun shareQuote(context: Context, quote: Quote) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "${quote.text}\n— ${quote.author}")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share quote via"))
}

// Favorite quotes persistence
private const val PREFS_NAME = "QuotePrefs"
private const val FAVORITES_KEY = "favorites"

fun loadFavorites(context: Context): List<Quote> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(FAVORITES_KEY, null)
    return if (json != null) {
        json.split("|").mapNotNull { line ->
            val parts = line.split("~")
            if (parts.size == 3) Quote(parts[0], parts[1], parts[2]) else null
        }
    } else {
        emptyList()
    }
}

fun saveFavorites(context: Context, quotes: List<Quote>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = quotes.joinToString("|") { "${it.text}~${it.author}~${it.date}" }
    prefs.edit().putString(FAVORITES_KEY, json).apply()
}

fun toggleFavorite(context: Context, currentFavorites: List<Quote>, quote: Quote): List<Quote> {
    return if (currentFavorites.any { it.text == quote.text }) {
        currentFavorites.filter { it.text != quote.text }.also {
            saveFavorites(context, it)
        }
    } else {
        (currentFavorites + quote).also {
            saveFavorites(context, it)
        }
    }
}

fun removeFavorite(context: Context, currentFavorites: List<Quote>, quote: Quote): List<Quote> {
    return currentFavorites.filter { it.text != quote.text }.also {
        saveFavorites(context, it)
    }
}
