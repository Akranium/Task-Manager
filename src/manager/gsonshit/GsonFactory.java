package manager.gsonshit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.Task;
import task.impl.OneTimeTask;
import task.impl.RecurringTask;

import java.time.LocalDate;

public class GsonFactory {
    public static Gson create() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapterFactory(
                        RuntimeTypeAdapterFactory
                                .of(Task.class, "type") // "type" is the field used to distinguish subtypes
                                .registerSubtype(OneTimeTask.class, "ONE_TIME")
                                .registerSubtype(RecurringTask.class, "RECURRING")
                )
                .setPrettyPrinting()
                .create();
        return gson;
    }
}
