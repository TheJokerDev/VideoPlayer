package me.j0keer.mediaplayer.config;

import com.google.common.base.Charsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class YamlConfiguration extends ConfigurationProvider
{

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>()
    {
        @Override
        protected Yaml initialValue() {

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );

            Representer representer = new Representer(options)
            {
                {
                    representers.put( Configuration.class, data -> represent( ( (Configuration) data ).self ));
                }
            };

            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setAllowDuplicateKeys( false );
            loaderOptions.setProcessComments(true);

            return new Yaml( new Constructor(loaderOptions), representer, options );
        }
    };

    @Override
    public void save(Configuration config, File file) throws IOException
    {
        try ( Writer writer = new OutputStreamWriter( new FileOutputStream( file ), Charsets.UTF_8 ) )
        {
            save( config, writer );
        }
    }

    @Override
    public void save(Configuration config, Writer writer)
    {
        yaml.get().dump( config.self, writer );
    }

    @Override
    public Configuration load(File file) throws IOException
    {
        return load( file, null );
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException
    {
        try ( FileInputStream is = new FileInputStream( file ) )
        {
            return load( is, defaults );
        }
    }

    @Override
    public Configuration load(Reader reader)
    {
        return load( reader, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( reader, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(InputStream is)
    {
        return load( is, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(InputStream is, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( is, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(String string)
    {
        return load( string, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( string, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }
}
