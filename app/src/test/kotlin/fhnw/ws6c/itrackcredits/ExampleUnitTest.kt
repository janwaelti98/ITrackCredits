package fhnw.ws6c.itrackcredits

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 *  ACHTUNG: Auf Android arbeiten wir (noch) mit JUNIT 4
 */

class ExampleUnitTest {

    @Test
    fun testJunitSetup(){
        //given
        val s1 = 1
        val s2 = 2

        //when
        val sum = s1 + s2

        //then
        assertEquals(3, sum)
    }
}