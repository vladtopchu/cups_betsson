package es.betsson.cups.utils

sealed class AppState(val message: String? = null) {
    class PrevCheck(): AppState()
    class ConnectionCheck(): AppState()
    class WebViewLogic(): AppState()
    class TargetLogic(): AppState()
    class PlugLogic(): AppState()
}