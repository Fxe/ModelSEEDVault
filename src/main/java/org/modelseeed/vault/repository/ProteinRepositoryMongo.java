package org.modelseeed.vault.repository;

import java.io.IOException;
import java.util.Set;

import org.bson.Document;
import org.bson.types.Binary;
import org.modelseeed.vault.core.Compress;
import org.modelseeed.vault.core.Protein;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Repository
public class ProteinRepositoryMongo {
    
    private final MongoCollection<Document> proteinCollection;
    
    // Standard amino acid vocabulary (20 standard amino acids plus common variants)
    private static final Set<Character> DEFAULT_PROTEIN_VOCAB = Set.of(
        'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 
        'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V',
        'U', 'O', 'B', 'Z', 'J', 'X', '*', '-'  // Extended amino acids and gap characters
    );
    
    public ProteinRepositoryMongo(MongoDatabase database, String proteinCollectionName) {
        this.proteinCollection = database.getCollection(proteinCollectionName);
    }
    
    /**
     * Validates a protein sequence against a vocabulary of allowed characters
     * @param sequence The protein sequence to validate
     * @param vocab Set of allowed characters (amino acids)
     * @return true if sequence is valid, false otherwise
     */
    public boolean validateSequence(String sequence, Set<Character> vocab) {
        if (sequence == null || sequence.isEmpty()) {
            return false;
        }
        
        // Convert to uppercase for validation
        String upperSequence = sequence.toUpperCase();
        
        for (char c : upperSequence.toCharArray()) {
            if (!vocab.contains(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates a protein sequence against the default amino acid vocabulary
     * @param sequence The protein sequence to validate
     * @return true if sequence is valid, false otherwise
     */
    public boolean validateSequence(String sequence) {
        return validateSequence(sequence, DEFAULT_PROTEIN_VOCAB);
    }
    
    /**
     * Stores a protein sequence after validation and compression
     * @param sequence The protein sequence to store
     * @return The SHA256 hash of the stored sequence
     * @throws IllegalArgumentException if sequence validation fails
     * @throws IOException if compression fails
     */
    public String storeSequence(String sequence) throws IOException {
        // Validate the sequence
        if (!validateSequence(sequence)) {
            throw new IllegalArgumentException("Invalid protein sequence: contains non-standard amino acid characters");
        }
        
        // Create protein object to get hash
        Protein protein = new Protein(sequence);
        String hash = protein.getHash();
        
        // Compress the sequence
        byte[] compressedSequence = Compress.compress(sequence);
        
        // Store in MongoDB
        Document doc = new Document("_id", hash)
                .append("z_seq", new Binary(compressedSequence))
                .append("original_length", sequence.length())
                .append("compressed_length", compressedSequence.length);
        
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
