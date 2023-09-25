package com.fisk.dataservice.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**

 * 构造目录JSON树

 * Created by fukang on 2017/5/26 0026.

 */

public class TreeBuilder {

    List<Node> nodes = new ArrayList<>();

    public JSONObject buildTree(List<Node> nodes) {

        TreeBuilder treeBuilder = new TreeBuilder(nodes);

        return treeBuilder.buildJSONTree();

    }

    public TreeBuilder() {

    }

    public TreeBuilder(List nodes) {

        super();

        this.nodes = nodes;

    }

// 构建JSON树形结构

    public JSONObject buildJSONTree() {

        JSONObject nodeTree = buildTree();
        return nodeTree;

    }

// 构建树形结构

    public JSONObject buildTree() {

        JSONObject treeNodes = new JSONObject();

        List<Node> rootNodes = getRootNodes();

        for (Node rootNode : rootNodes) {

            buildChildNodes(rootNode);
            JSONObject jsonParameter = (JSONObject) rootNode.getParameter();
            Iterator<Map.Entry<String, Object>> parameter = jsonParameter.entrySet().iterator();
            while (parameter.hasNext()) {
                Map.Entry<String, Object> next = parameter.next();
                treeNodes.put(next.getKey(),next.getValue());
            }
        }

        return treeNodes;

    }

// 递归子节点

    public void buildChildNodes(Node node) {

        List<Node> children = getChildNodes(node);
        List<Object> list = new ArrayList<>();
        JSONObject json = new JSONObject();
        if (!children.isEmpty()) {
            JSONObject childJson = new JSONObject();
            for (Node child : children) {
                buildChildNodes(child);
                list.add(child.getParameter());
                JSONObject jsonParameter = (JSONObject) child.getParameter();
                Iterator<Map.Entry<String, Object>> parameter = jsonParameter.entrySet().iterator();
                while (parameter.hasNext()) {
                    Map.Entry<String, Object> next = parameter.next();
                    childJson.put(next.getKey(),next.getValue());
                }
            }
            JSONObject jsonNode = (JSONObject) node.getParameter();
            Iterator<Map.Entry<String, Object>> parameter = jsonNode.entrySet().iterator();
            while (parameter.hasNext()) {
                Map.Entry<String, Object> next = parameter.next();
                json.put(next.getKey(),childJson);
            }
            node.setParameter(json);

        }

    }

// 获取父节点下所有的子节点

    public List getChildNodes(Node pnode) {

        List childNodes = new ArrayList<>();

        for (Node n : nodes) {

            if (pnode.getId().equals(n.getPid())) {

                childNodes.add(n);

            }

        }

        return childNodes;

    }

// 判断是否为根节点

    public boolean rootNode(Node node) {

        boolean isRootNode = true;

        for (Node n : nodes) {

            if (node.getPid().equals(n.getId())) {

                isRootNode = false;

                break;

            }

        }

        return isRootNode;

    }

// 获取集合中所有的根节点

    public List getRootNodes() {

        List rootNodes = new ArrayList<>();

        for (Node n : nodes) {

            if (rootNode(n)) {

                rootNodes.add(n);

            }

        }

        return rootNodes;

    }

    public static class Node {

        private String id;

        private String pid;

        private Object parameter;

        public Node() {

        }

        public Node(String id, String pid) {

            super();

            this.id = id;

            this.pid = pid;

        }

        public String getId() {

            return id;

        }

        public void setId(String id) {

            this.id = id;

        }

        public String getPid() {

            return pid;

        }

        public void setPid(String pid) {

            this.pid = pid;

        }

        public Object getParameter() {

            return parameter;

        }

        public void setParameter(Object parameter) {

            this.parameter = parameter;

        }

    }

}