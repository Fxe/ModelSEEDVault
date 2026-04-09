package org.modelseeed.vault.neo4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Export {
  
  private static final ObjectMapper MAPPER = new ObjectMapper();
  
  public record ExportGraph(List<List<Object>> rowsNodes, List<List<Object>> rowsEdges) {};
  
  public static ExportGraph exportRelationships(Transaction tx, Iterable<RelationshipType> export) {
    
    Set<String> nodeEids = new HashSet<>();
    List<List<Object>> rowsNodes = new ArrayList<>();
    List<List<Object>> rowsEdges = new ArrayList<>();

      
      for (RelationshipType type: export) {
        try (ResourceIterator<Relationship> it = tx.findRelationships(type)) {
          while(it.hasNext()) {
            Relationship r = it.next();
            
            String src = r.getStartNode().getElementId();
            String dst = r.getEndNode().getElementId();
            rowsEdges.add(List.of(src, r.getType().name(), dst, r.getAllProperties()));
            nodeEids.add(src);
            nodeEids.add(dst);
          }
        };

      }
      
      for (String eid: nodeEids) {
        Node node = tx.getNodeByElementId(eid);
        Set<String> labels = new HashSet<>();
        for (Label l : node.getLabels()) {
            labels.add(l.name());
        }
        rowsNodes.add(List.of(node.getElementId(), labels, node.getAllProperties()));
      }
    
    
    return new ExportGraph(rowsNodes, rowsEdges);
  }
  
  public static void writeToJson(ExportGraph g, OutputStream os) {
    try {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(os, g);
    } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON", e);
    }
  }
  
  public static void writeToFile(ExportGraph g, File file) {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      writeToJson(g, fos);
  } catch (IOException e) {
      throw new RuntimeException("Failed to write file", e);
  }
  }
  
}
