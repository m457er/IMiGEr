package cz.zcu.kiv.offscreen.servlets.api;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import cz.zcu.kiv.offscreen.api.Graph;
import cz.zcu.kiv.offscreen.graph.GraphManager;
import cz.zcu.kiv.offscreen.graph.loader.GraphJSONDataLoader;
import cz.zcu.kiv.offscreen.graph.loader.JSONConfigLoader;
import cz.zcu.kiv.offscreen.servlets.BaseServlet;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is used for loading diagrams from session.
 */
public class GetSessionDiagram extends BaseServlet {

    /**
     * Constructs graph data using either the current graph version or the version set in query parameter. Resulting
     * graph is returned as JSON in response body.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        getDiagramFromSession(request, response);
    }

    /**
     * Add file which was uploaded and is stored in session to response or set http status code to BAD_REQUEST.
     */
    private void getDiagramFromSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonToDisplay = (String) request.getSession().getAttribute("json_graph");
        String jsonType = (String) request.getSession().getAttribute("json_graph_type");
        String filename = (String) request.getSession().getAttribute("graph_filename");

        if (!Strings.isNullOrEmpty(jsonToDisplay) && jsonType != null) {

            String rawJson;

            switch (jsonType) {
                case "spade":
                    rawJson = convertSpadeToRawJson(jsonToDisplay);
                    break;
                default:
                    rawJson = jsonToDisplay;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("graph_json", rawJson);
            jsonObject.addProperty("name", filename);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonObject.toString());
            response.getWriter().flush();
            return;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Convert input spade JSON to frontend backend JSON and return it.
     */
    private String convertSpadeToRawJson(String spadeJson) {
        GraphManager graphManager = new GraphJSONDataLoader(spadeJson).LoadData();
        JSONConfigLoader configLoader = new JSONConfigLoader(graphManager);

        Graph graph = graphManager.createGraph(configLoader);
        JSONObject json = JSONObject.fromObject(graph);

        return json.toString();
    }
}
