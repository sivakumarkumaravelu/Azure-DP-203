package com.gssystems.movielens.cogsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;

public class SearchMovies {
	private static final String INDEX_NAME = "movielens";

	private static Properties loadPropertiesFromResource(String resourcePath) throws IOException {
		InputStream inputStream = CreateMovieLensIndex.class.getResourceAsStream(resourcePath);
		Properties configProperties = new Properties();
		configProperties.load(inputStream);
		return configProperties;
	}

	public static void main(String[] args) throws Exception {
		searchForMovies("genres", "comedy");
	}

	public static List<MovieDTO> searchForMovies(String searchBasis, String textData) {
		List<MovieDTO> toReturn = new ArrayList<MovieDTO>();

		try {
			Properties config = loadPropertiesFromResource("/com/gssystems/movielens/cogsearch/config.properties");
			String endPoint = config.getProperty("ENDPOINT");
			String serviceName = config.getProperty("SearchServiceName");
			String searchServiceQueryKey = config.getProperty("SearchServiceQueryKey");

			SearchClient searchClient = new SearchClientBuilder().endpoint(String.format(endPoint, serviceName))
					.credential(new AzureKeyCredential(searchServiceQueryKey)).indexName(INDEX_NAME).buildClient();

			SearchOptions options = new SearchOptions().setSearchFields(searchBasis).setTop(5);

			SearchPagedIterable results = searchClient.search(textData, options, Context.NONE);

			for (SearchResult searchResult : results) {
				SearchDocument doc = searchResult.getDocument(SearchDocument.class);
				String title = (String) doc.get("title");
				String tagline = (String) doc.get("tagline");
				String isAdult = (String) doc.get("is_adult");
				String overview = (String) doc.get("overview");
				String homepage = (String) doc.get("homepage");
				Double runtime = (Double) doc.get("runtime");
				String poster_path = (String) doc.get("poster_path");
				String imdb_id = (String) doc.get("imdb_id");
				
				MovieDTO aDto = new MovieDTO();
				aDto.setTitle(title);
				aDto.setTagLine(tagline);
				aDto.setHomePage(homepage);
				aDto.setIsAdult(isAdult);
				aDto.setOverview(overview);
				aDto.setRuntime(runtime);
				aDto.setImdbId(imdb_id);
				aDto.setPoster_path(poster_path);
				
				toReturn.add(aDto);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return toReturn;
	}

	public static final Map<String, List<String>> getFacets() {
		Map<String, List<String>> toReturn = new TreeMap<String, List<String>>();

		try {
			Properties config = loadPropertiesFromResource("/com/gssystems/movielens/cogsearch/config.properties");
			String endPoint = config.getProperty("ENDPOINT");
			String serviceName = config.getProperty("SearchServiceName");
			String searchServiceQueryKey = config.getProperty("SearchServiceQueryKey");

			SearchClient searchClient = new SearchClientBuilder().endpoint(String.format(endPoint, serviceName))
					.credential(new AzureKeyCredential(searchServiceQueryKey)).indexName(INDEX_NAME).buildClient();

			SearchOptions options = new SearchOptions().setFacets("cast", "original_language", "genres", "keywords")
					.setTop(1);

			SearchPagedIterable results = searchClient.search("*", options, Context.NONE);

			Map<String, List<FacetResult>> facets = results.getFacets();
			Iterator<String> keys = facets.keySet().iterator();
			while (keys.hasNext()) {
				String aKey = keys.next();
				List<String> facetVals = new ArrayList<String>();
				List<FacetResult> fr = facets.get(aKey);
				for (FacetResult aFr : fr) {
					facetVals.add(aFr.getAdditionalProperties().get("value") + "");
				}

				toReturn.put(aKey, facetVals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return toReturn;
	}
}
