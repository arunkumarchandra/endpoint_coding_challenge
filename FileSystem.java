import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSystem {

    private static final String SPACE = " ";

    private final DirectoryNode root;

    public FileSystem() {
        this.root = new DirectoryNode("/");
    }

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        fs.runCommand("CREATE fruits");
        fs.runCommand("CREATE vegetables");
        fs.runCommand("CREATE grains");
        fs.runCommand("CREATE fruits/apples");
        fs.runCommand("CREATE fruits/apples/fuji");
        fs.runCommand("LIST");
        fs.runCommand("CREATE grains/squash");
        fs.runCommand("MOVE grains/squash vegetables");
        fs.runCommand("CREATE foods");
        fs.runCommand("MOVE grains foods");
        fs.runCommand("MOVE fruits foods");
        fs.runCommand("MOVE vegetables foods");
        fs.runCommand("LIST");
        fs.runCommand("DELETE fruits/apples");
        fs.runCommand("DELETE foods/fruits/apples");
        fs.runCommand("LIST");
    }

    private void runCommand(String command) {
        final var commandArray = command.split(SPACE);
        final var verb = commandArray[0];
        switch (verb) {
            case "CREATE" -> createDirectory(command.substring(verb.length() + 1));
            case "MOVE" -> moveDirectory(command.substring(verb.length() + 1));
            case "DELETE" -> deleteDirectory(command.substring(verb.length() + 1));
            case "LIST" -> listDirectories();
            default -> System.out.println("unknown verb: " + verb);
        }
    }

    private void createDirectory(String path) {
        var current = root;
        final var components = path.split("/");
        for (String component : components) {
            var child = current.getChild(component);
            if (child == null) {
                child = new DirectoryNode(component);
                current.addChild(child);
            }
            current = child;
        }
        System.out.println("CREATE " + path);
    }

    private void moveDirectory(String path) {
        final var components = path.split(SPACE);
        final var sourcePath = components[0];
        final var targetPath = components[1];
        final var sourcePathArray = sourcePath.split("/");
        final var sourceDir = sourcePathArray[sourcePathArray.length - 1];
        final var targetDir = targetPath.split("/")[0];
        final var sourceNode = findNode(root, sourceDir);
        final var targetNode = findNode(root, targetDir);
        if (sourceNode == null || targetNode == null) {
            System.out.println("source or target directory not found");
            return;
        }
        final var sourceParent = getParent(root, sourceNode);
        // detach source from source parent
        if (sourceParent != null) {
            sourceParent.children.remove(sourceNode);
        }
        // add source node as children to target node
        targetNode.children.add(sourceNode);
        System.out.println("MOVE " + path);
    }

    private DirectoryNode findNode(DirectoryNode node, String name) {
        if (node.name.equals(name)) {
            return node;
        }
        for (DirectoryNode child : node.children) {
            final var result = findNode(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private DirectoryNode getParent(DirectoryNode root, DirectoryNode child) {
        if (root == null) {
            return null;
        }
        for (DirectoryNode node : root.children) {
            if (node == child) {
                return root;
            } else {
                final var result = getParent(node, child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void deleteDirectory(String path) {
        System.out.println("DELETE " + path);
        final var pathArray = path.split("/");
        final var headDir = pathArray[0];
        final var tailDir = pathArray[pathArray.length - 1];
        DirectoryNode node = null;
        for (DirectoryNode child : root.children) {
            if (child.name.equals(headDir)) {
                node = child;
                break;
            }
        }
        if (node == null) {
            System.out.println("Cannot delete " + path + " - " + headDir + " does not exist");
            return;
        }
        final var tailNode = findNode(root, tailDir);
        final var tailParent = getParent(root, tailNode);
        if (tailParent != null) {
            tailParent.children.remove(tailNode);
        }
    }

    private void listDirectories() {
        System.out.println("LIST");
        final var children = root.children;
        Collections.sort(children);
        for (DirectoryNode child : children) {
            listDirectories(child, 0);
        }
    }

    private void listDirectories(DirectoryNode node, int prefixSpacesCount) {
        if (node == null) {
            return;
        }
        final var directory = SPACE.repeat(Math.max(0, prefixSpacesCount)) + node.name;
        System.out.println(directory);
        final var children = node.children;
        Collections.sort(children);
        for (DirectoryNode child : children) {
            listDirectories(child, prefixSpacesCount + 2);
        }
    }

    static class DirectoryNode implements Comparable<DirectoryNode> {
        private final String name;
        private final List<DirectoryNode> children;

        public DirectoryNode(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        public void addChild(DirectoryNode child) {
            this.children.add(child);
        }

        private DirectoryNode getChild(String name) {
            for (DirectoryNode child : children) {
                if (child.name.equals(name)) {
                    return child;
                }
            }
            return null;
        }

        @Override
        public int compareTo(DirectoryNode node) {
            return this.name.compareTo(node.name);
        }
    }

}
