package me.j0keer.mediaplayer.util;

import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.config.Configuration;
import me.j0keer.mediaplayer.config.ConfigurationProvider;
import me.j0keer.mediaplayer.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class FileUtils {
    private File file;
    private Configuration configuration;

    public FileUtils(Main mod, String path, String fileName){
        file = new File(mod.getDataFolder()+path, fileName);
        init();
    }

    public FileUtils(Main mod, String fileName){
        file = new File(mod.getDataFolder(), fileName);
        init();
    }

    public FileUtils(File file){
        this.file = file;
        init();
    }

    public void init(){
        if (file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public Object get(String key){
        return get(key, null);
    }
    public <T> T get(String key, T def){
        return configuration.get(key, def);
    }

    public String getString(String key){
        return getString(key, null);
    }
    public String getString(String key, String def){
        return configuration.getString(key, def);
    }

    public List<String> getStringList(String key){
        return configuration.getStringList(key);
    }

    public List<?> getList(String key){
        return getList(key);
    }

    public List<?> getList(String key, List<?> def){
        return configuration.getList(key, def);
    }

    public boolean getBoolean(String key){
        return getBoolean(key, false);
    }
    public boolean getBoolean(String key, boolean def){
        return configuration.getBoolean(key, def);
    }

    public Integer getInt(String key){
        return getInt(key, 0);
    }
    public Integer getInt(String key, int def){
        return configuration.getInt(key, def);
    }

    public double getDouble(String key){
        return getDouble(key, 0.0D);
    }
    public double getDouble(String key, double def){
        return configuration.getDouble(key, def);
    }

    public float getFloat(String key){
        return getFloat(key, 0F);
    }
    public float getFloat(String key, float def){
        return configuration.getFloat(key, def);
    }

    public Configuration getSection(String key){
        return configuration.getSection(key);
    }

    public Collection<String> getKeys(){
        return configuration.getKeys();
    }

    public void set(String key, Object value){
        configuration.set(key, value);
    }

    public void reload(){
        save();
        init();
    }
}
