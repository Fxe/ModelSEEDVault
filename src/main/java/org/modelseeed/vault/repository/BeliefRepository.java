package org.modelseeed.vault.repository;

import java.io.IOException;

import org.bson.Document;
import org.modelseeed.vault.core.ProteinSequence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class BeliefRepository {
  
  private final MongoCollection<Document> profileCollection;
  
  public BeliefRepository(MongoDatabase database, String profileCollectionName) {
      this.profileCollection = database.getCollection(profileCollectionName);
  }
  
  public Object getProfile(String profileId) throws IOException {
    this.profileCollection.find(new Document("_id", profileId));
    return null;
  }
}
