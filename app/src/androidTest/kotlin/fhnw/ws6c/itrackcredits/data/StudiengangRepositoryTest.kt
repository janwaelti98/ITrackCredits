package fhnw.ws6c.itrackcredits.data

import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert

internal class StudiengangRepositoryTest {

    @Test
    fun testLoadData() {
        // given
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        //when
        StudiengangRepository.loadStudiengang(appContext)

        //then
        StudiengangRepository.data.apply {
            Assert.assertEquals("iCompetence", get(0).name)
            Assert.assertEquals("Informatik", get(1).name)
            Assert.assertEquals(12, get(0).modulgruppen.size)
            Assert.assertEquals(11, get(1).modulgruppen.size)
            Assert.assertEquals(6, get(0).modulgruppen[1].modules.size)
            Assert.assertEquals(6, get(1).modulgruppen[1].modules.size)
        }
    }
}