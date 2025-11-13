package es.uniovi.sdm.buscarciudades.data

class Resultados(val franja: Int = 30000) {
    var puntos= 0
        private set

    fun addPuntos(distancia: Float) {
        when {
            distancia < franja ->     puntos+= 10
            distancia < franja * 2 -> puntos+= 5
            distancia < franja * 3 -> puntos+= 2
        }
    }

    fun reiniciaPuntos() {
        puntos= 0
    }
}