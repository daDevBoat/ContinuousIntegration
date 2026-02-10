package ci;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Status class for persisting commit history.
 *
 * <p>It is a Springboot service, which makes it visible to a class by passing it in its
 * constructor.
 */
@Service
public class Status {

  /** Record that represents a single commit and its associated information. */
  public record CommitRecord(String sha, String state, String time, String message) {
    ;
  }

  // Latest commit
  private final AtomicReference<CommitRecord> latest = new AtomicReference<>();
  // Persisted commits
  private final ConcurrentHashMap<String, CommitRecord> commits = new ConcurrentHashMap<>();
  private final ObjectMapper mapper;
  // Path to "database"
  private final Path commitsFilePath;

  /** This is the constructor that Springboot will use to create the Status service class. */
  public Status() {
    this(Paths.get("build", "commits.json"));
  }

  public Status(Path commitsFilePath) {
    this.commitsFilePath = commitsFilePath;
    this.mapper = new ObjectMapper();
  }

  /**
   * Instructions for Springboot when initiating the Status class. If the function encounters an
   * error when reading from file or parsing json, no commit history is loaded.
   *
   * <p>Loads persisted commits from json file.
   */
  @PostConstruct
  public void init() {
    try {
      String json = readJsonFromFile();
      if (!json.isBlank()) {
        loadFromJson(json);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Function returns the latest persisted commit.
   *
   * @return Latest persisted commit.
   */
  public Optional<CommitRecord> getLatest() {
    return Optional.ofNullable(latest.get());
  }

  /** Returns the map of commits. Used for lookup by SHA. */
  public ConcurrentHashMap<String, CommitRecord> getCommitsMap() {
    return this.commits;
  }

  /**
   * Function that returns the commit history as a list of commits.
   *
   * @return commit history sorted after time descending
   */
  public List<CommitRecord> getCommits() {
    return commits.values().stream()
        .sorted(Comparator.comparing(CommitRecord::time).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Function that persists a given commit. If an exception is caught when writing to JSON it is
   * ignored, and the commit will not be stored on file.
   *
   * @param status commit to persist
   */
  public void put(CommitRecord status) {
    latest.set(status);
    commits.put(status.sha(), status);
    try {
      writeJsonToFile(mapToJson(commits));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads commit history from json into the commit map.
   *
   * @param json json to load commit history from
   * @throws JsonMappingException json parsing exception
   * @throws JsonProcessingException json parsing exception
   */
  private void loadFromJson(String json) throws JsonMappingException, JsonProcessingException {
    commits.clear();
    commits.putAll(jsonToMap(json));

    latest.set(
        commits.values().stream().max(Comparator.comparing(CommitRecord::time)).orElse(null));
  }

  /**
   * Attempts to convert given json string to a Map with SHA as key and commit reference as value.
   *
   * @param json json to read from
   * @return Map with SHA as key and commit reference as value
   * @throws JsonMappingException json parsing exception
   * @throws JsonProcessingException json parsing exception
   */
  private Map<String, CommitRecord> jsonToMap(String json)
      throws JsonMappingException, JsonProcessingException {
    if (json == null || json.isBlank()) {
      return Map.of();
    }

    return mapper.readValue(
        json,
        new TypeReference<Map<String, CommitRecord>>() {
          ;
        });
  }

  /**
   * Attempts to convert a Map with SHA as key and commit reference as value to a json string.
   *
   * @param map map to convert into json
   * @return json representation of the given map
   * @throws JsonProcessingException json conversion exception
   */
  private String mapToJson(Map<String, CommitRecord> map) throws JsonProcessingException {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
  }

  /**
   * Attempts to read json from given file.
   *
   * @return json found in file
   * @throws IOException if an exception was met when attempting to read from file
   */
  private String readJsonFromFile() throws IOException {
    if (!Files.exists(commitsFilePath)) {
      return "";
    }
    return Files.readString(commitsFilePath, StandardCharsets.UTF_8);
  }

  /**
   * Writes json to file.
   *
   * @param json json to persist in file
   * @throws IOException if an exception was met when attempting to write to file
   */
  private void writeJsonToFile(String json) throws IOException {
    Files.createDirectories(commitsFilePath.getParent());
    Files.writeString(
        commitsFilePath,
        json,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, // if not exists
        StandardOpenOption.TRUNCATE_EXISTING, // overwrite existing
        StandardOpenOption.WRITE); // open for write
  }
}
