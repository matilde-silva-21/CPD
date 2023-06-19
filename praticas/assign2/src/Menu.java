public class Menu {
    public static String LOGIN_MENU =
            "@Welcome to Wordle!@" +
            "@1 - Register@"+ "2 - Login with username and password@"+ "3 - Login with token@" +
            "@Please choose your option: ";

    public static String QUEUE_MENU =
            "@1 - Enter Queue"+
            "@2 - Show Score and Rank" +
            "@3 - Rules" +
            "@4 - Log Out" +
            "@Please choose your option: ";

    public static String RULES =
                    "@Wordle is a challenging and addictive word-guessing game where your goal is to guess a hidden" +
                    "@five-letter word within a limited number of attempts. Sharpen your vocabulary and logical" +
                    "@thinking skills as you strive to crack the code!@" +
                    "@Rules:" +
                    "@1. You have six attempts to guess the hidden word correctly." +
                    "@2. The hidden word consists of five letters, and each letter can be any lowercase English alphabet." +
                    "@3. After each guess, you will receive feedback on your guess:" +
                    "@   a. A letter in green if it is both in the correct position and in the hidden word." +
                    "@   b. A letter in yellow if it is in the hidden word but in a different position." +
                    "@   c. No indication if the letter is not in the hidden word." +
                    "@4. Use the feedback to deduce the correct letters and their positions in the word." +
                    "@5. Guess the word within the six attempts to win the game!@" +
                    "@Ranked System:" +
                    "@  Your performance in Wordle is ranked based on your score, which is@" +
                    "@determined by the number of attempts taken to guess the word correctly." +
                    "@  The rank system rewards your accuracy and efficiency:" +
                    "@      • DIAMOND Rank: Score above 15000." +
                    "@      • PLATINUM Rank: Score between 14999 and 9000." +
                    "@      • GOLD Rank: Score between 8999 and 5000." +
                    "@      • SILVER Rank: Score between 4999 and 2000." +
                    "@      • BRONZE Rank: Score below 1999. Rookie!@" +
                    "@The faster and more accurate you are in uncovering the hidden word, the higher your rank will be." +
                    "@Compete with friends, challenge yourself, and strive to achieve the Perfect Rank in every game!" +
                    "@Are you ready to test your word-guessing skills? Let the Wordle challenge begin!";

    public static String AUTHENTICATION_MENU_INPUT_ERROR = "Invalid Option. Please choose 1, 2 or 3: ";
    public static String QUEUE_MENU_INPUT_ERROR = "Invalid Option. Please choose 1 or 2: ";
    public static String REGISTER_ACTION_USERNAME_ERROR = "Username already exists.";
    public static String ACTION_ERROR = "Server couldn't process request.";
    public static String LOGIN_ACTION_USERNAME_ERROR = "Username does not exist.";
    public static String LOGIN_ACTION_PASSWORD_ERROR = "Password is incorrect.";
    public static String LOGIN_ACTION_ERROR = "Silly goose, you're already logged in!";
    public static String REGISTER_SUCCESS = "Registration successful! You are now logged in.";
    public static String LOGIN_SUCCESS = "Login successful!";
    public static String LOGOUT_SUCCESS = "Hope you will be back soon...";
    public static String TOKEN_FAIL = "Sorry, token does not exist.";
    public static String TOKEN_SUCCESS = "Token login successful. Welcome Back!";
    public static String TOKEN_REQUEST = "Please enter your token: ";
    public static String TOKEN_SEND = "Here is your token, don't loose it! ";
    public static String ENTER_QUEUE_SUCCESS = "You are now in the queue! Please wait until there are enough players.";
    public static String GAME_OVER = "GAME OVER";

    public static String PING = "Pinging...";
}
