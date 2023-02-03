package fhnw.ws6c.itrackcredits

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import fhnw.ws6c.EmobaApp
import fhnw.ws6c.itrackcredits.data.StudiengangRepository
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import ITrackCreditsUI
import androidx.room.Room
import fhnw.ws6c.itrackcredits.data.AppDatabase
import fhnw.ws6c.itrackcredits.data.Converters


object ITrackCreditsApp : EmobaApp {
    private lateinit var model : ITrackCreditsModel

    override fun initialize(activity: ComponentActivity) {
        StudiengangRepository.loadStudiengang(activity)
        val converterInstance = Converters()
        val db = Room.databaseBuilder(
            activity,
            AppDatabase::class.java, "ITrackCreditsApp-db"
        ).addTypeConverter(converterInstance)
            .fallbackToDestructiveMigration()
            .build()
        model = ITrackCreditsModel(StudiengangRepository, db, activity)
        model.loadPrefs()
        model.loadDataFromDB()
    }

    @Composable
    override fun CreateUI() {
        ITrackCreditsUI(model)
    }

}

