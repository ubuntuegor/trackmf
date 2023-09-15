package to.bnt.trackmf.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @FilesDir
    @Provides
    fun provideFilesDir(@ApplicationContext context: Context): File = context.filesDir
}
