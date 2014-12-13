var app = angular.module('myApp', []);

app.controller('MyCtrl', function($scope) {
	
	$('#mainpage').height($(window).height() + 50);
		$('#output').height($(window).height() + 50);
		$('#stats').height($(window).height() + 50);	
	
		$scope.hello = "hello world";
		$scope.showDummyImages = true;
		$scope.videoURLS = "";
		$scope.inputText = "";
		$scope.timeTaken = "";
		
		$scope.finalFrameLocation = "https://sincre-data.s3.amazonaws.com/FinalFrames/";
		$scope.finalVideoLocation = "https://sincre-data.s3.amazonaws.com/Videos/";
		
		$scope.clickedVideo = function(url){
			console.log("clicked" + url);
			var player = document.getElementById('videoPlayer');
		    var mp4Vid = document.getElementById('mp4Source');
		    player.pause();
     	    $(mp4Vid).attr('src', url);
		    player.load();
		    player.play();
		};
		
	$(window).load(function() {
			 
		    var fileName = "old";
			var uploadFiles = function() {
	            dialog.dialog("close");
	            //var files = document.getElementById('file-select').files;
	            
	            var data = new FormData();
	            data.append("file", document.getElementById("file-select").files[0]);
	            data.append("file", document.getElementById("file-select").files[0]);    
	            
	            $scope.inputText = document.getElementById("file-select").files[0].name;
	            
	            $("#ready").hide();
	            $("#loading").show('slow');
	            
	            $("#status").innerHTML = "Uploading and Processing File ...";
	            $.ajax({
	                url : 'rest/files/upload',
	                type : 'POST',
	                data : data,
	                cache : false,
	                dataType : 'json',
	                processData : false, // Don't process the files
	                contentType : false, // Set content type to false as jQuery will tell the server its a query string request
	                success : function(data, textStatus, jqXHR) {
	                    $("#loading").hide('slow');
	                    $("#ready").show('slow');
	                    if (typeof data.error === 'undefined') {
	                        // Success so call function to process the form
	                    	console.log($scope.inputText);
	                    	console.log(fileName);
	                    	//$scope.inputText = fileName;
	                        fileName = data;
	                        console.log(fileName);
	                        console.log($scope.hello);
	                        console.log($scope.inputText);
	                        //submitForm(event, data);
	                    } else {
	                        // Handle errors here
	                        console.log('ERRORS: ' + data.error);
	                    }
	                },
	                error : function(jqXHR, textStatus, errorThrown) {
	                    // Handle errors here
	                    $("#loading").hide('slow');
	                    $("#ready").show('slow');
	                    console.log('Rest failed ERRORS: ' + textStatus);
	                    // STOP LOADING SPINNER
	                }
	            });
	            $scope.$digest();
	        };
	        
	        var searchFiles = function(){
	        	console.log("searchFilesCalled");
	        	//var d = new Date();
	        	var n1 = new Date().getTime();
	            
	        	$.ajax({
	                url : 'rest/files/getMatches/' + fileName,
	                type : 'GET',
	                cache : false,
	                dataType : 'json',
	                processData : false, // Don't process the files
	                contentType : false, // Set content type to false as jQuery will tell the server its a query string request
	                success : function(data, textStatus, jqXHR) {
	                    $("#loading").hide('slow');
	                    $("#ready").hide('slow');
	                    if (typeof data.error === 'undefined') {
	                        // Success so call function to process the form
	                    	console.log(data);
	                    	console.log($scope.hello);
	                    	$scope.timeTaken = (new Date().getTime() - n1)/1000;
	                    	loadingVideo.dialog("close");
	                    	
	                    	$("#ready").hide('slow');
	                        $("#loading").hide('slow');
	            			$('html,body').animate({
	            				scrollTop : $("#output").offset().top + 80
	            			}, 'slow'); 
	                    	
	                    	
	                    	$scope.showDummyImages = false;
	                    	$scope.videoURLS = JSON.parse(data);
	                    	for(var i=0;i<$scope.videoURLS.length;i++){
	                    		var temp = $scope.videoURLS[i].summary.split("\\");
		                    	$scope.videoURLS[i].frame = $scope.finalFrameLocation + temp[temp.length-2]+ "/"+temp[temp.length-1];
		                    	console.log($scope.videoURLS[i].frame);
		                    	$scope.videoURLS[i].video = $scope.finalVideoLocation + temp[temp.length-2].substring(0,temp[temp.length-2].length - 3) + "mp4";
		                    	console.log($scope.videoURLS[i].video);
		                    	console.log($scope.videoURLS[i]);
	                    	}
	                    	
	                    	$scope.$digest();
	                        //submitForm(event, data);
	                    } else {
	                        // Handle errors here
	                        console.log('ERRORS: ' + data.error);
	                    }
	                },
	                error : function(jqXHR, textStatus, errorThrown) {
	                    // Handle errors here
	                    $("#loading").hide('slow');
	                    $("#ready").show('slow');
	                    console.log('Rest failed ERRORS: ' + textStatus);
	                    // STOP LOADING SPINNER
	                }
	            });
	        };
			
		var dialog = $("#dialog-form").dialog({
			autoOpen : false,
			height : 155,
			width : 450,
			modal : true,
			buttons : {
				"upload" : uploadFiles,
				Cancel : function() {
					dialog.dialog("close");
				}
			},
			close : function() {
			}
		});
		
		var loadingVideo = $("#loading-form").dialog({
			autoOpen : false,
			height : 155,
			width : 450,
			modal : true,
			buttons : {
				
			},
			close : function() {
			}
		});

		/*var form = dialog.find("form").on("submit", function(event) {
			//event.preventDefault();
		});*/
		$("#upload").click(function() {
			$("#ready").hide('slow');
            $("#loading").hide('slow');
			dialog.dialog("open");
		});
		
		$("#search").click(function() {
			searchFiles();
			loadingVideo.dialog("open");
		});

		$('html,body').animate({
			scrollTop : 0
		}, 'slow');
		
		$('.flexslider').flexslider({
			animation : "slide",
			animationLoop : false,
			itemWidth : 210,
			itemMargin : 5,
			pausePlay : true,
			start : function(slider) {
				$('body').removeClass('loading');
			}
		});
		
		
	});

	////////////////////////////
	
});


