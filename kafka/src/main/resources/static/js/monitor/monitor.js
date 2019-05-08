
Highcharts.setOptions({
    // 纠正时区误差
    global: {
        timezoneOffset: -8 * 60
    },
    lang: {
        loading: '数据加载中...'  // 加载中文字配置
    }
});

var warningWaterLevel = 80.0;   //预警水位
var errorWaterLevel = 90.0;     //危险水位

// 定义实时图表
var chartReal = Highcharts.chart('real-chart', {
    chart: {
        type: 'spline',
        animation: {
            duration: 50
        }
    },
    title: {
        text: '实时监控'
    },
    credits: {
        enabled: false
    },
    loading: {  // 加载中选项配置
        labelStyle: {
            color: 'red',
            fontSize: '12px'
        }
    },
    xAxis: {
        type: 'category'
    },
    yAxis: {
        title: {
            text: 'm'
        },
        min: 20,
        max: 100,
        plotBands: [{ // 水位警戒区域
            from: warningWaterLevel,
            to: errorWaterLevel,
            color: 'rgba(255, 185, 15, 0.1)',
            label: {
                text: '预警水位',
                style: {
                    color: '#8B8989'
                }
            }
        }, { // Light air
            from: errorWaterLevel,
            to: 100,
            color: 'rgba(255, 69, 0, 0.1)',
            label: {
                text: '危险水位',
                style: {
                    color: '#8B8989'
                }
            }
        }]
    },
    tooltip: {
        valueSuffix: ' m'
    },
    series: [{
        name: 'HoHai',
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        pointStart: Date(),
        pointInterval:  3000    //间隔为3秒
    }]
});

//开启数据加载动画
chartReal.showLoading();
var chartRealIsShowLoading = true;

//存储chartReal显示的json数据
var chartRealSeriesDatas = [];



// 单个数据更新RealChart
var updateChartReal = function (data){
    updateSeriesDatas(chartRealSeriesDatas, data);
    chartReal.series[0].update({'data': chartRealSeriesDatas});

    //当水位低于预警水位，取消预警
    if (data.waterLevel < warningWaterLevel
        && chartReal.series[0].color !== '#7cb5ec'){
        chartReal.series[0].update({'color': '#7cb5ec'});
    }

    //水位预警
    if (data.waterLevel >= warningWaterLevel && data.waterLevel < errorWaterLevel){
        chartReal.series[0].update({'color': '#FFD700'});
        toastr.warning("水位达预警线", "警告");
    } else if (data.waterLevel >= errorWaterLevel){
        chartReal.series[0].update({'color': '#FF0000'});
        toastr.error("水位达危险线", "危险");
    }
};

// 数组数据更新RealChart
var updateChartRealByArray = function(datas){
    for (var i in datas){
        updateSeriesDatas(chartRealSeriesDatas, datas[i]);
    }
    console.info("chartRealSeriesDatas = " + JSON.stringify(chartRealSeriesDatas));
    chartReal.series[0].update({'data': chartRealSeriesDatas});
};

var updateSeriesDatas = function(seriesDatas, data){
        var record = {};
        var date = new Date(data.time);
        record["name"] = date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
        record["y"] = data.waterLevel;
        seriesDatas.push(record);
        if (seriesDatas.length > 20){
            seriesDatas.shift();
        }
        // return seriesDatas;
    };

    <!-- 滑动条 -->
var lastIndex = 0;
$(function(){
    $("#real-chart-slider").slider({
        range: "max",
        min: 0,
        max: 60,
        value: 60,
        slide: function(event, ui) {
            lastIndex = parseInt(60) - parseInt(ui.value);
            console.info("ui.max = " + parseInt(60) + " ui.value = " + parseInt(ui.value) + " index = " + parseInt(lastIndex));

            // 更新回显数据
            $("#notification-bar").val(lastIndex);
        }
    });
    // 设置notification-bar显示初始值
    $("#notification-bar").val(0);
});

//将需要回溯的时间戳发送给后端
var backTrackingServiceStarted = false;
var sendSliderTimestampByIndex = function(index){
    if (!backTrackingServiceStarted)
    {
        socket.send(JSON.stringify(["subscribe_back_tracking_service"]));
        backTrackingServiceStarted = false;
        console.info("订阅backTrackingService");
    }

    var time;
    if (index === 0){
        console.info("back_tracking_done");
        time = getTimestamp(index);
        socket.send(JSON.stringify(["unsubscribe_back_tracking_service", time]));
        backTrackingServiceStarted = false;
    }else{
        time = getTimestamp(index);
        socket.send(JSON.stringify(["back_tracking", time]));
    }

};

//根据index计算时间戳
var getTimestamp = function(index){
    var time =  new Date(new Date().getTime() -  index * 60 * 1000).getTime();    //获取index 分钟之前的时间
    var date = new Date(time);
    console.info("timestamp = " + time + " date = " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + " now date = " + new Date());
    return time;
};

//重置滑动条，移至开始位置
var resetSlider = function(){
    $("#real-chart-slider").slider({
        value: 60
    });
    $("#notification-bar").val(0);
};



// 定义daily-chart图表
var chartDaily = Highcharts.chart('daily-chart', {
    chart: {
        type: 'spline',
        animation: {
            duration: 50
        },
        height: 300
    },
    title: {
        text: '日监控'
    },
    credits: {
        enabled: false
    },
    loading: {  // 加载中选项配置
        labelStyle: {
            color: 'red',
            fontSize: '12px'
        }
    },
    xAxis: {
        type: 'category'
    },
    yAxis: {
        title: {
            text: 'm'
        },
        min: 20,
        max: 100
    },
    tooltip: {
        valueSuffix: ' m'
    },
    series: [{
        name: 'HoHai',
        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }]
});

//realchart数据列
var chartDailySeriesDatas = [];

//data为单条数据，刷新图表增加一个点
function updateChartDaily(data){
    updateChartDailySeriesDatas(chartDailySeriesDatas, data);
    chartDaily.series[0].update({'data': chartDailySeriesDatas});
}

function updateChartDailyByArray(datas){
    for (var i in datas){
        updateChartDailySeriesDatas(chartDailySeriesDatas, datas[i]);
    }
    chartDaily.series[0].update({'data': chartDailySeriesDatas});
}

//接受数组数据，批量刷新图表
function updateChartDailySeriesDatas(seriesDatas, data){
    var record = {};
    var date = new Date(data.time);
    record["name"] = date.getHours() + "h";
    record["y"] = data.waterLevel;
    seriesDatas.push(record);
    if (seriesDatas.length > 24){
        seriesDatas.shift();
    }
}



// 定义weekly-chart图表
var chartWeekly = Highcharts.chart('weekly-chart', {
    chart: {
        type: 'spline',
        animation: {
            duration: 50
        },
        height: 300
    },
    title: {
        text: '周监控'
    },
    credits: {
        enabled: false
    },
    loading: {  // 加载中选项配置
        labelStyle: {
            color: 'red',
            fontSize: '12px'
        }
    },
    xAxis: {
        type: 'category'
    },
    yAxis: {
        title: {
            text: 'm'
        },
        min: 20,
        max: 100
    },
    tooltip: {
        valueSuffix: ' m'
    },
    series: [{
        name: 'HoHai',
        data: ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    }]
});

var chartWeeklySeriesDatas = [];

function updateChartWeekly(data){
    updateChartWeeklySeriesDatas(chartWeeklySeriesDatas, data);
    chartWeekly.series[0].update({'data': chartWeeklySeriesDatas});
}

function updateChartWeeklyByArray(datas){
    for (var i in datas){
        updateChartWeeklySeriesDatas(chartWeeklySeriesDatas, datas[i]);
    }
    chartWeekly.series[0].update({'data': chartWeeklySeriesDatas});
}

function updateChartWeeklySeriesDatas(seriesDatas, data){
    var record = {};
    var date = new Date(data.time);
    var day = date.getDay();
    var dayOfWeeky;
    if (day === 0){
        dayOfWeeky = '周日';
    } else if (day === 1){
        dayOfWeeky = '周一';
    } else if (day === 2){
        dayOfWeeky = '周二';
    } else if (day === 3){
        dayOfWeeky = '周三';
    } else if (day === 4){
        dayOfWeeky = '周四';
    } else if (day === 5){
        dayOfWeeky = '周五';
    } else if (day === 6){
        dayOfWeeky = '周六';
    } else {
        console.info("dayOfWeek error");
    }
    record["name"] = dayOfWeeky;
    record["y"] = data.waterLevel;
    seriesDatas.push(record);
    if (seriesDatas.length > 7){
        seriesDatas.shift();
    }
}

var back = function(){
    console.info("调用back()");
    socket.send("back");
};

var real = function(){
    console.info("调用real()");
    socket.send("real");
};



<!-- 弹出通知栏toastr配置 -->
toastr.options = {
        closeButton: false,
        debug: false,
        progressBar: false,
        positionClass: "toast-top-center",
        onclick: null,
        showDuration: "300",
        hideDuration: "1000",
        timeOut: "2000",
        extendedTimeOut: "1000",
        newestOnTop: true,
        showEasing: "swing",
        hideEasing: "linear",
        showMethod: "fadeIn",
        hideMethod: "fadeOut"};

//更新预警水位和危险水位
function updateWarrningValue(){
    //刷新前端水位数值显示
   warningWaterLevel = document.getElementById('warnning-value').value;
   document.getElementById("warrning-display").innerText = warningWaterLevel;
   updateChartPlotBands();
}

function updateErrorValue(){
    errorWaterLevel = document.getElementById('error-value').value;
    document.getElementById("error-display").innerText = errorWaterLevel;
    updateChartPlotBands();
}

function updateChartPlotBands(){
    chartReal.yAxis[0].update(
        {'plotBands' :
                [{ // 水位警戒区域
                    from: warningWaterLevel,
                    to: errorWaterLevel,
                    color: 'rgba(255, 185, 15, 0.1)',
                    label: {
                        text: '预警水位',
                        style: {
                            color: '#8B8989'
                        }
                    }
                }, { // Light air
                    from: errorWaterLevel,
                    to: 100,
                    color: 'rgba(255, 69, 0, 0.1)',
                    label: {
                        text: '危险水位',
                        style: {
                            color: '#8B8989'
                        }
                    }}]
        })
}

//定义socket连接
var socket;
function ws() {
    if (window.WebSocket) {

        socket = new WebSocket("ws://localhost:8080/webSocket");
        // socket = new WebSocket("ws://192.168.42.183:8080/webSocket");

        socket.onmessage = function (event) {
            var datas = JSON.parse(event.data);
            if (datas[0] === "real_monitor"){
                if (datas[1] instanceof Array){
                    console.info("收到数组数据");
                    updateChartRealByArray(datas[1]);
                }else{
                    console.info("收到数据： record( timestmap = " + datas[1].timestamp + ", id = " + datas[1].id + ", siteName = " + datas[1].siteName + ", waterLevel = " + datas[1].waterLevel);
                    updateChartReal(datas[1]);
                }

                //关闭chartReal的数据加载动画
                if (chartRealIsShowLoading){
                    chartReal.hideLoading();
                    chartRealIsShowLoading = false
                }
            }else if(datas[0] === "daily_monitor"){
                if (datas[1] instanceof Array){
                    updateChartDailyByArray(datas[1]);
                }else{
                    updateChartDaily(datas[1]);
                    console.info("update chartDaily data = " + JSON.stringify(datas[1]));
                }

            }else if(datas[0] === "weekly_monitor"){
                if (datas[1] instanceof Array) {
                    updateChartWeeklyByArray(datas[1]);
                }else{
                    updateChartWeekly(datas[1]);
                }
            }else if(datas[0] === "monthly_monitor"){
                if (datas[1] instanceof Array){
                    updateChartMonthlyByArray(datas[1]);
                }else{
                    updateChartMonthly(datas[1]);
                }

            }else if (datas[0] === "back_tracking_done"){
                //后台的BackTrackingListener关闭时，将chart-slider滑快位置初始化为最右端
                resetSlider();
            } else{
                alert("error(reponsetype = " + datas[0] + ")");
            }

        };

        socket.onopen = function (event) {
            console.info("连接开启");

            //订阅实时监控
            socket.send(JSON.stringify(["subscribe_real_monitor_service"]));

            //订阅日监控
            socket.send(JSON.stringify(["subscribe_daily_monitor_service"]));

            //订阅周监控预
            socket.send(JSON.stringify(["subscribe_weekly_monitor_service"]));

        };

        socket.onclose = function (event) {
            console.info("连接被关闭");
        };
    } else {
        alert("你的浏览器不支持 WebSocket！");
    }

}

//建立socket连接，等待服务器“推送”数据，用回调函数更新图表
$(document).ready(function() {
    //开始建立WebSocket传输数据
    ws();
});


