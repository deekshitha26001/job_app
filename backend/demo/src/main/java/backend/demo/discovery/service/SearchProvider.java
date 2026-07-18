package backend.demo.discovery.service;

import java.util.List;

public interface SearchProvider {
    List<String> search(String query);
}
