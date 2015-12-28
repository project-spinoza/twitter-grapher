$(document).ready(function(){

var isPostProcessing = false;
var nodesCount = 0;


// $('.slider-container').on('click', function(){

// alert('pasa marha');

// });

var queryPanelDisplayed = true;
$( "#submitQueryFormShowHide" ).click(function() {

  $( ".submitQueryForm" ).animate({
    opacity: 1,
    left: "+=50",
    height: "toggle"
  }, 1500, function() {

    if (queryPanelDisplayed === true) {
      $('#submitQueryFormShowHide img').attr("src","images/arrow-down.png");
        queryPanelDisplayed = false;
    }else {
        $('#submitQueryFormShowHide img').attr("src","images/arrow-up.png");
        queryPanelDisplayed = true;
    }
  });
});


var nodePanelDisplayed = false;
$( "#nodeInfoPanel a#nodeInfoShowHide" ).click(function() {
  $( "#nodeInfo" ).animate({
    opacity: 1,
    left: "+=50",
    width: "toggle"
  }, 1, function() {

    if (nodePanelDisplayed === true) {
      $("#nodeInfoPanel a#nodeInfoShowHide img").attr("src","images/arrow-right.png");
        nodePanelDisplayed = false;
    }else {
        $("#nodeInfoPanel a#nodeInfoShowHide img").attr("src","images/arrow-left.png");
        nodePanelDisplayed = true;
    }
  });
});

$('#nodeInfoPanel a#nodeInfoShowHide').click(function(e) {
    e.preventDefault();
});
$('#submitQueryFormShowHide').click(function(e) {
    e.preventDefault();
});


$('#datasource').on('change', function() {
	if(this.value == "graphfile")
	{
		$("#searchField").attr('disabled', 'disabled');
		isGraphfile = true;
	} 
	else{
		$("#searchField").removeAttr("disabled");
		isGraphfile = false;		
	}
});

$('.nodecentrality,.pagerank,.neighborcount').jRange({
		from: 0,
		to: 100,
		step: 1,
		scale: [0,25,50,75,100],
		format: '%s',
		width: 300,
		snap : true,
    ondragend: function() {
      if ($('#onFlyChanges').is(":checked") && nodesCount > 0) {
              isPostProcessing = true;
             $('#clickbtn').trigger("click");
      }
    }
});
  
  sigma.classes.graph.addMethod('neighbors', function(nodeId) {
  var k,
  neighbors = {},
  index = this.allNeighborsIndex[nodeId] || {};
  for (k in index)
    neighbors[k] = this.nodesIndex[k];
    return neighbors;
  });
  
function emptyfieldbordercolor(value){
     $(value).css('border','2px solid red');
     return false;     
}
  
function nonemptyfieldbordercolor(value){
    $(value).css('border','');
}

document.body.style.backgroundColor = color;

$('#clickbtn').click(function(event) {

    event.preventDefault();
//  Field Values
	  var searchFieldValue = '';
    var nodecentralityValue = $(".nodecentrality").val();
    var pagerankthreshholdValue = $(".pagerank").val();
	  var neighborcount = $(".neighborcount").val();
    var layoutValue = $('#layout_type').val();
    var nodesizebyvalue = $('#nodesizeby').val();
    var datasource = $('#datasource').val();

    if (!isPostProcessing) {
       searchFieldValue = $("#searchField").val();
    } else {
       searchFieldValue = 'postProcessingVal';
    }

    if(datasource == '' | datasource == null){
    		datasource = null;
        emptyfieldbordercolor('#datasource');
    } else{
        nonemptyfieldbordercolor('#datasource')
    } 

    if(searchFieldValue=='' && datasource != "graphfile"){

        emptyfieldbordercolor('#searchField');
    } else{
		    nonemptyfieldbordercolor('#searchField');
	  }

	
	/*<![CDATA[*/
    if((searchFieldValue!='' && datasource != null) || isGraphfile){
       $("#loader_img").css("visibility", "visible");
       var request_url = "";

       if (!isPostProcessing){

          request_url = "http://localhost:8080/ajax";
       }else {
          request_url = "http://localhost:8080/processGraph";
          isPostProcessing = false;
       }
       $.ajax({
        type: "get",
        url: request_url,
        data:{searchField:searchFieldValue,nc:nodecentralityValue,prt:pagerankthreshholdValue,layouttype:layoutValue,NodeSizeBy:nodesizebyvalue,NeighborCountRange:neighborcount,datasource:datasource},
        async : true, beforeSend: function(xhr) {},
        //on successfull ajax request
        success: function (graphData) {
           nodesObject = JSON.parse(graphData);
           if (nodesObject === undefined || nodesObject === null) {
           	  $(".message").html("Error generating graph from given Inputs");
          	  $(".message").css("display","block");
          	  $("#container").empty();
           }
           nodesCount = nodesObject.nodes.nodes.length;
           if(nodesObject.nodes.nodes.length > 0){
      	      $(".message").css("display","none");
      	      showGraph(nodesObject.nodes, document.getElementById('container'), Gsetting);
      	   } else{
      	      $(".message").html("No data available for given terms");
      	      $(".message").css("display","block");
      	      $("#container").empty();
      	   }
           $("#loader_img").css("visibility", "hidden");
        },
        //on error in ajax request
        error: function(a, b, c){
        	 $(".message").html("Internal server error");
      	   $(".message").css("display","block");
    	     $("#container").empty();
           $("#loader_img").css("visibility", "hidden");
        }
    });
    }/*]]>*/

});

function onclick(searchFieldValue){
	   searchFieldValue = "null";
	   $("#searchField").prop('disabled', true);
	   return searchFieldValue;
}

function showGraph(givenData, givenContainer, givenSettings){
    givenContainer.innerHTML = "";
    s = new sigma( {
            graph : givenData,
            renderer: {
              container:givenContainer,
              type: 'canvas'
            },
            settings:givenSettings
          });

     totalEdges = s.graph.edges();

   for (var i in totalEdges) {
 	   totalEdges[i].type = 'curve';
 	 }

   s.graph.nodes().forEach(function(n) {
      n.originalColor = n.color;
      n.originalLabel = n.label;
   });

   s.graph.edges().forEach(function(e) {
     e.originalColor = e.color;
   });

   s.bind('clickEdge rightClickEdge', function(e) {
     console.log(e);
   });

   s.bind('overNode', function(e){

      $("#nodeInfoTable").empty();
      //papulate table
      var attr = e.data.node.attributes;
      var row = "<tr><td>" + 'ID:' + "</td><td>" + e.data.node.id; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'Label:' + "</td><td>" + e.data.node.label; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'OriginalLabel:' + "</td><td>" +  e.data.node.originalLabel; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'Color:' + "</td><td>" + e.data.node.color; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'B/w Centrality:' + "</td><td>" + attr["Betweenness Centrality"]; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'Closeness Centrality:' + "</td><td>" + attr["Closeness Centrality"]; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'PageRank:' + "</td><td>" + attr["PageRank"]; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'NeighborCount:' + "</td><td>" + attr["NeighborCount"]; + "</td></tr>";
      $('#nodeInfoTable').append(row);
      var row = "<tr><td>" + 'Eccentricity:' + "</td><td>" + attr["Eccentricity"]; + "</td></tr>";
      $('#nodeInfoTable').append(row);

      var nodeId = e.data.node.id;
      toKeep = s.graph.neighbors(nodeId);
      toKeep[nodeId] = e.data.node;

      s.graph.nodes().forEach(function(n) {

        if (toKeep[n.id]){
          n.color = n.originalColor;
          n.label = n.originalLabel;
        } else{
          n.color = 'blue';
          n.label = "";
        }
    });

    s.graph.edges().forEach(function(e) {
      if (toKeep[e.source] && toKeep[e.target])
        e.color ='green';
      else
       e.color = e.originalColor;
    });
    s.refresh();
  });

  s.refresh();
   }
 });  