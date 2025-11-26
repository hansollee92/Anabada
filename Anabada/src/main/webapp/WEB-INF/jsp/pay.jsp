<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="pay-container" style="max-width:560px;margin:60px auto;background:#fff;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.06);padding:32px;">
  <h1 style="margin:0 0 10px;font-size:25px;font-weight:700;">카드결제</h1>
  <p style="margin:0 0 28px;color:#189a7e;font-size:13px;">주문 정보를 확인해주세요.</p>

  <c:if test="${not empty error}">
    <p style="color:#d9534f;margin-top:-12px;margin-bottom:18px;"><c:out value="${error}"/></p>
  </c:if>

  <!-- 인풋 대신 텍스트만 출력 -->
  <div style="display:grid;gap:12px;font-size:15px;">
    <div>
      <div style="color:#666;margin-bottom:4px;font-size:16px;font-weight:700;">주문명</div>
      <div style="margin-bottom: 8px;"><c:out value="${PrdName}"/></div>
    </div>
    <div>
      <div style="color:#666;margin-bottom:4px;font-size:16px;font-weight:700;">결제금액</div>
      <div style="margin-bottom: 8px;"><c:out value="${Price}"/> 원</div>
    </div>
    <div>
      <div style="color:#666;margin-bottom:4px;font-size:16px;font-weight:700;">주문자명</div>
      <div style="margin-bottom: 10px;"><c:out value="${MemberName}"/></div>
    </div>
  </div>

  <div style="display:flex;gap:10px;margin-top:24px;">
    <button id="btnCard" style="flex:1;height:44px;border:none;border-radius:8px;background:#0C8274;color:#fff;font-weight:700;cursor:pointer;">
      카드결제
    </button>
    <button type="button" onclick="history.back()" style="flex:1;height:44px;border:none;border-radius:8px;background:#EEEEEE;color:#333;font-weight:700;cursor:pointer;">
      취소
    </button>
  </div>
</div>

<script src="https://js.tosspayments.com/v1"></script>
<script>
  (function(){
    const ctx = '<c:out value="${ctx}"/>';
    const tossPayments = TossPayments('');

    // 서버에서 내려준 값
    const orderName     = '<c:out value="${PrdName}"/>';
    const amount        = Number('<c:out value="${Price}"/>');
    const customerName  = '<c:out value="${MemberName}"/>';
    const prdNo         = '<c:out value="${prdNo}"/>';

    document.getElementById('btnCard').addEventListener('click', function(){
      const orderId = 'order-' + Date.now();

      const successUrl =
        window.location.origin + ctx + '/paySuccess.do'
        + '?orderName=' + encodeURIComponent(orderName || '주문')
        + '&customerName=' + encodeURIComponent(customerName || '고객')
        + '&prdNo=' + encodeURIComponent(prdNo);

      tossPayments.requestPayment('카드', {
        amount: amount,
        orderId: orderId,
        orderName: orderName || '주문',
        customerName: customerName || '고객',
        successUrl: successUrl,
        failUrl:    window.location.origin + ctx + '/payFail.do'
      }).catch(function (error) {
        console.warn(error);
        alert('결제가 취소되었거나 실패했습니다.');
      });
    });
  })();
</script>
