package xyz.haoshoku.nick.website;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import xyz.haoshoku.nick.api.NickConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MineSkinGetter {

    private static final LoadingCache<String, String[]> skinByURLCache = CacheBuilder.newBuilder().expireAfterWrite( 24, TimeUnit.HOURS )
            .build( new CacheLoader<String, String[]>() {
                @Override
                public String[] load( String url ) {
                    try {
                        HttpPost post = new HttpPost( "https://api.mineskin.org/generate/url" );
                        post.addHeader( "User-Agent", NickConfig.getMineSkinUserAgent() );
                        post.addHeader( "Authorization", "Bearer " + NickConfig.getMineSkinAPIKey() );

                        HttpEntity httpEntity = MultipartEntityBuilder.create()
                                .addTextBody( "text", "{\"url\":\"" + url + "\",\"visibility\":0,\"name\":\"\",\"variant\":\"classic\"}" ).build();
                        post.setEntity( httpEntity );

                        try ( CloseableHttpClient client = HttpClients.createDefault() ) {
                            InputStream stream = client.execute( post ).getEntity().getContent();
                            BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
                            return MineSkinGetter.getSkinTextureFromJSON( reader );
                        }
                    } catch ( Exception ignore ) {
                        return NickConfig.getDefaultSkin();
                        // Ignoring because if people sets random urls, so the console do not get spammed
                    }
                }
            } );

    public static String[] skinTextureByURL( String url ) {
        try {
            return MineSkinGetter.skinByURLCache.get( url );
        } catch ( ExecutionException e ) {
            return NickConfig.getDefaultSkin();
        }
    }

    private static final LoadingCache<File, String[]> skinByFileCache = CacheBuilder.newBuilder().expireAfterWrite( 30, TimeUnit.MINUTES )
            .build( new CacheLoader<File, String[]>() {
                @Override
                public String[] load( File file ) {
                    try {
                        HttpPost post = new HttpPost( "https://api.mineskin.org/generate/upload" );
                        post.addHeader( "User-Agent", NickConfig.getMineSkinUserAgent() );
                        post.addHeader( "Authorization", "Bearer " + NickConfig.getMineSkinAPIKey() );
                        HttpEntity entity = MultipartEntityBuilder.create()
                                .addPart( "file", new FileBody( file ) ).build();
                        post.setEntity( entity );
                        try ( CloseableHttpClient client = HttpClients.createDefault() ) {
                            InputStream stream = client.execute( post ).getEntity().getContent();
                            BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
                            return MineSkinGetter.getSkinTextureFromJSON( reader );
                        }
                    } catch ( Exception e ) {
                        return NickConfig.getDefaultSkin();
                    }
                }
            } );

    public static String[] skinTextureByFile( File file ) {
        try {
            return MineSkinGetter.skinByFileCache.get( file );
        } catch ( ExecutionException e ) {
            return NickConfig.getDefaultSkin();
        }
    }

    private static String[] getSkinTextureFromJSON( BufferedReader reader ) {
        JsonElement dataElement = new JsonParser().parse( reader ).getAsJsonObject().get( "data" );
        JsonElement textureElement = dataElement.getAsJsonObject().get( "texture" );
        String value = String.valueOf( textureElement.getAsJsonObject().get( "value" ) ).replace( "\"", "" );
        String signature = String.valueOf( textureElement.getAsJsonObject().get( "signature" ) ).replace( "\"", "" );
        return new String[]{ value, signature };
    }

}
