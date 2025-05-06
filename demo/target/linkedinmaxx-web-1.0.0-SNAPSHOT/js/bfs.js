// bfs.js

/**
 * Breadth-First Search to find shortest path between two nodes in a graph.
 *
 * @param {Object} graph - Adjacency list: { node: [neighbors] }
 * @param {string} from - Starting node
 * @param {string} to - Destination node
 * @returns {Object} { distance: number, path: array of nodes }
 */
 function bfs(graph, from, to) {
    if (!graph[from] || !graph[to]) return { distance: -1, path: [] };
  
    const queue = [[from]];
    const visited = new Set([from]);
  
    while (queue.length > 0) {
      const path = queue.shift();             // Current path
      const node = path[path.length - 1];     // Last node in the path
  
      if (node === to) {
        return {
          distance: path.length - 1,
          path: path
        };
      }
  
      for (const neighbor of graph[node] || []) {
        if (!visited.has(neighbor)) {
          visited.add(neighbor);
          queue.push([...path, neighbor]);
        }
      }
    }
  
    return { distance: -1, path: [] }; // No connection found
  }
  
  module.exports = bfs;
  