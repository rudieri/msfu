package br.com.msfu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author rudieri
 */
public class GitHub {
    public static void main(String[] args) throws IOException {
        
        GitHubClient client = new GitHubClient();
//        client.setCredentials("rudieri", "mestre1024arara");
        
//        RepositoryService repServ = new RepositoryService(client);
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("q", "language:Java");
//        params.put("sort", "stars");
//        params.put("order", "desc");
//        List<Repository> repositories = repServ.getRepositories(params);
//        for (Repository rep : repositories) {
//                System.out.println(rep.getName() + ": " + rep.getLanguage());
//            
//        }
//        PageIterator<Repository> pageAllRepositories = repServ.pageAllRepositories();
//        for (Iterator<Collection<Repository>> iterator = pageAllRepositories.iterator(); iterator.hasNext();) {
//            Collection<Repository> next = iterator.next();
//            for (Repository rep : next) {
//                System.out.println(rep.getName() + ": " + rep.getLanguage());
//            }
//        }
        
//        repositoryService.getRepositories(null)
        GitHubRequest req = new GitHubRequest();
        req.setUri("/search/repositories");
//        req.setType(new TypeToken<Map<String, String>>() {
//		}.getType());
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("q", "language:Java");
        params.put("sort", "stars");
        params.put("order", "desc");
        req.setParams(params);
        String generateUri = req.generateUri();
//        GitHubResponse get = client.get(req);
//        System.out.println(generateUri);
//        InputStream stream = client.getStream(req);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//        String linha;
////        while ((linha = reader.readLine()) != null) {
////            System.out.println(linha);
////        }
//        Object fromJson = GsonUtils.fromJson(reader, new TypeToken<Abacate>(){}.getType());
//        System.out.println(fromJson);
        int count = 0;
        req.setType(Abacate.class);
        GitHubResponse resp = client.get(req);
        String next = null;
        do {
            Abacate abacate = (Abacate) resp.getBody();
            for (Repository item : abacate.items) {
                System.out.println(item.getName() + ";" + item.getCloneUrl() + ";" + item.getSize() + ";" + item.getMasterBranch() + ";" + item.getForks());                
                count++;
            }
            if (next != null) {
                req.setUri(new URL(next).getFile());
                resp = client.get(req);
                
            }
        } while ((next = resp.getNext()) != null && count < 400);
//        String first = resp.getFirst();
        
//        System.out.println(first);
//        Object body = resp.getBody();
//        System.out.println(body);
    }
    private static class Abacate implements Serializable{
        public List<Repository> items;
    }
}
