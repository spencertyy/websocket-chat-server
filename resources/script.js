//make by: yuyaotu  HW8  Week4/Day16/WebChat/
"use strict"
const chatRoom =document.getElementById("chat-room");
const chatMessages = document.getElementById("chat-messages");
const messageInput =document.getElementById("message-input");
const sendButton = document.getElementById("send-button")
const userName = document.getElementById("username");
const roomNumber = document.getElementById("roomNumber");
const joinButton = document.getElementById("joinBtn");
const leaveButn =document.getElementById("leave-button");
let wsOpen = false;

let ws = new WebSocket("ws://localhost:8080");
ws.onopen=handleOpenCB;
// 获取当前日期和时间
const currentTime = new Date();

// 格式化日期和时间
let formattedTime = currentTime.toLocaleString(); // 可以使用其他格式化方式来满足需求

function handleOpenCB(){
    wsOpen = true;
    console.log("WebSocket connection is open.");
}
ws.onmessage = (event) =>{
    console.log("recieve data: "+event.data);
    let data = JSON.parse(event.data);
    //event.data 中的 JSON 数据解析成 JavaScript 对象，并将该对象存储在 data 变量中，以便后续在代码中使用。
    console.log("receive parse data: " + data);//console.log 来输出变量的值、查看函数的执行结果
    //ws.send(data);

    if (data.type === "join") {
        const joinTheRoom = document.createElement("div");
        joinTheRoom.textContent=`(${data.time}) ${data.user} has joined the ${data.room} `;
        chatRoom.appendChild(joinTheRoom);

    }
    else if (data.type === "leave") {
        const leaveTheRoom = document.createElement("div");
        leaveTheRoom.textContent=`(${data.time})  ${data.user} leave the room `;
        chatRoom.appendChild(leaveTheRoom);
    }
    if(data.type == "message")  {
        let messageDiv= document.createElement("p")
        messageDiv.textContent = `(${data.time})  ${data.user}:${data.message}`;
        console.log("Message form "+data.message);
        chatMessages.appendChild(messageDiv);

    }

}
// Add an event listener for the send button
sendButton.addEventListener("click", sendMessage);
joinButton.addEventListener("click", joinRoom);
leaveButn.addEventListener("click", leaveTheRoom);

function sendMessage() {
    //let user = userName.value;
    const messageText = messageInput.value;
    //console.log("user"+ user + "massage"+ messageText);
    if (messageText != "") {//if the message is not empty
        let message =
            {
            "user"    : userName.value,
            "type"    : "message",
            "message" : messageInput.value,
             "time"   : formattedTime // Adds a timestamp to the message
            };
        //console.log("user"+ user + "massage"+ messageText);
        //console.log(JSON.stringify(message));
        ws.send(JSON.stringify(message));
    }
}
// function sanitize( string ) {
//     const map = {
//         '&': '&amp;', '<': '&lt;’, '>': '&gt;',
//         '"': '&quot;', "'": '&#x27;', "/": '&#x2F;',
//     };
//     const reg = /[&<>"'/]/ig;
//     return string.replace(reg, (match)=>(map[match]));
// }

function joinRoom() {
    console.log("join");//print the join to see is work or not
    //let user = userName.value;
    let roomName = roomNumber.value;
    // 使用正则表达式检查是否只包含小写字母
    const isLowerCaseOnly = /^[a-z]+$/.test(roomName);
    if (!isLowerCaseOnly) {
        alert("Invalid room name. Please use only lowercase letters.");
    } else {
        let message =
            {
            "user"    : userName.value,
            "type"    : "join" ,
            "room"    : roomNumber.value,
            "time"    : formattedTime
            };

        //console.log("User: "+user+" join the Room: "+roomName)
        ws.send(JSON.stringify(message));

    }
}
function leaveTheRoom(){
    let message =
        {
            "user"    : userName.value,
            "type"    : "leave" ,
            "room"    : roomNumber.value,
            "time"    : formattedTime
        };
    // console.log("User: "+user+ " leave the room")
    ws.send(JSON.stringify(message));
}
