package org.modelseeed.vault.repository;

import java.io.IOException;

import org.bson.Document;
import org.bson.types.Binary;
import org.modelseeed.vault.core.Compress;
import org.modelseeed.vault.core.ProteinSequence;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Repository
public class ProteinRepositoryMongo {
    
    private final MongoCollection<Document> proteinCollection;
    
    public ProteinRepositoryMongo(MongoDatabase database, String proteinCollectionName) {
        this.proteinCollection = database.getCollection(proteinCollectionName);
    }
    
    /**
     * Stores a protein sequence after validation and compression
     * @param sequence The protein sequence to store
     * @return The SHA256 hash of the stored sequence
     * @throws IllegalArgumentException if sequence validation fails
     * @throws IOException if compression fails
     */
    public String storeSequence(ProteinSequence protein) throws IOException {
        // Validate the sequence
        //if (!ProteinSequence.validateSequence(sequence, ProteinSequence.DEFAULT_PROTEIN_VOCAB)) {
        //    throw new IllegalArgumentException("Invalid protein sequence: contains non-standard amino acid characters");
        //}
        
        // Create protein object to get hash
        //ProteinSequence protein = ProteinSequence.buildFromSequence(sequence);
        String hash = protein.getHash();
        
        // Compress the sequence
        String sequence = protein.getSequence();
        byte[] compressedSequence = Compress.compress(protein.getSequence());
        
        // Store in MongoDB
        Document doc = new Document("_id", hash)
                .append("z_seq", new Binary(compressedSequence))
                .append("original_length", sequence.length())
                .append("compressed_length", compressedSequence.length);
        
        System.out.println(proteinCollection.countDocuments());
        
        //proteinCollection.insertOne(doc);
        
        // Use upsert to avoid duplicate key errors
        proteinCollection.replaceOne(
            new Document("_id", hash),
            doc,
            new com.mongodb.client.model.ReplaceOptions().upsert(true)
        );
        
        return hash;
    }
    
    /**
     * Retrieves a compressed sequence by its hash
     * @param hash The SHA256 hash of the sequence
     * @return The compressed sequence as Binary or null if not found
     */
    public Binary getCompressedSequence(String hash) {
        Document doc = proteinCollection.find(new Document("_id", hash)).first();
        return doc != null ? doc.get("z_seq", Binary.class) : null;
    }
    
    
    
    /**
     * Retrieves and decompresses a sequence by its hash
     * @param hash The SHA256 hash of the sequence
     * @return The original sequence or null if not found
     * @throws IOException if decompression fails
     */
    public String getSequence(String hash) throws IOException {
        Binary compressedSequence = getCompressedSequence(hash);
        return compressedSequence != null ? Compress.decompress(compressedSequence) : null;
    }
}
