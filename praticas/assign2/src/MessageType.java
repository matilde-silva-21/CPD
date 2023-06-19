enum MessageType {
    SERVER_ERROR,
    BEGIN_CONNECTION, ESTABLISHED_CONNECTION,
    AUTHENTICATION_MENU_USER_INPUT,
    LOGIN, LOGIN_SUCCESS, LOGIN_FAIL,
    REGISTER, REGISTER_SUCCESS, REGISTER_FAIL,
    TOKEN_LOGIN, TOKEN_FAIL, TOKEN_SUCCESS,
    VIEW_RANK, RULES, VIEW_RANK_SUCCESS,
    QUEUE_MENU, QUEUE_MENU_USER_INPUT, ENTER_QUEUE, ENTER_QUEUE_FAIL, ENTER_QUEUE_SUCCESS,
    TOKEN_LOGOUT, LOGOUT_SUCCESS, LOGOUT_FAIL,
    PLAY, GAME_OVER,
    PING
}