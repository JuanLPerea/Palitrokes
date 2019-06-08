package com.game.palitrokes.Modelos;

public class JugadaCom {

    public static String jugadaCom(Tablero tablero) {

        String jugadaSalida = null;

        // Convertir cada monton a un número binario
        // Sumar los números resultantes y obtener un número decimal
        // Si alguno de los dígitos del número decimal es par
        // quitar tantos palos como queramos para que quede impar
        // si no hay ningun número par es que llevamos todas las de perder
        // Y hacemos una jugada cualquiera con la esperanza
        // de que el otro meta la gamba




        // Recorrer todos los montones
        // Cada montón lo convertimos a un String con el formato "101010"
        // Vamos sacando cada caracter de el array de Strings
        // y lo sumamos con los de la misma posicion de los otros montones
        // guardamos en un String los montones que sean pares
        // Si hay uno solo, sacamos un número aleatorio (impar) con el largo de la cadena
        // y 1 para que sea impar
        // Creamos una cadena para luego separar con Split que sea
        // 'Numero de monton'#'Numero de palos a quitar'



        return jugadaSalida;

    }
}
