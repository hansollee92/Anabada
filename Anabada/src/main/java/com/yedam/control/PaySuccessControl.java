package com.yedam.control;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yedam.common.Control;
import com.yedam.service.ProductService;
import com.yedam.service.ProductServiceImpl;
import com.yedam.service.PurchaseService;
import com.yedam.service.PurchaseServiceImpl;
import com.yedam.vo.MemberVO;

public class PaySuccessControl implements Control {
    private static final String SECRET_KEY = "";

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String paymentKey   = req.getParameter("paymentKey");
        String orderId      = req.getParameter("orderId");
        String amount       = req.getParameter("amount");

        // 우리가 successUrl에 붙여 보낸 값
        String orderName    = req.getParameter("orderName");     // 상품명
        String customerName = req.getParameter("customerName");  // 구매자
        String prdNoStr     = req.getParameter("prdNo");         // 구매테이블 인서트용 상품번호

        if (paymentKey == null || orderId == null || amount == null) {
            req.setAttribute("error", "필수 파라미터가 누락되었습니다.");
            req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
            return;
        }

        String apiUrl = "https://api.tosspayments.com/v1/payments/confirm";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            String auth = Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%s}",
                                        paymentKey, orderId, amount);

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.write(body.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();

            // 성공이면 우리가 원하는 값들을 JSP로 전달
            if (code == 200) {
                // memberNo는 세션에서
                HttpSession session = req.getSession(false);
                MemberVO logMember = (MemberVO) session.getAttribute("logMember");
                int memberNo = logMember.getMemberNo();

                // prdNo는 쿼리스트링에서
                int prdNo = 0;
                try {
                    prdNo = Integer.parseInt(prdNoStr);
                } catch (Exception e) {
                    req.setAttribute("error", "상품번호가 올바르지 않습니다.");
                    req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
                    return;
                }

                // 구매 테이블 insert
                PurchaseService psvc = new PurchaseServiceImpl();
                boolean saved = psvc.recordPurchase(memberNo, prdNo);
                if (!saved) {
                    req.setAttribute("error", "구매내역 저장에 실패했습니다.");
                    req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
                    return;
                }
                
                // 상태를 '판매완료'로 변경
                ProductService prodSvc = new ProductServiceImpl();
                boolean updated = prodSvc.productSaleStatus("판매완료", prdNo);
                if (!updated) {
                    req.setAttribute("error", "상품 상태 변경에 실패했습니다.");
                    req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
                    return;
                }             

                // 성공 페이지로 값 전달
                req.setAttribute("orderName", orderName);
                req.setAttribute("amount", amount);
                req.setAttribute("customerName", customerName);
                req.getRequestDispatcher("/member/pay_success.tiles").forward(req, resp);

            } else {
                req.setAttribute("error", "승인 실패(code=" + code + ")");
                req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
            }

        } catch (Exception e) {
            req.setAttribute("error", "서버 오류: " + e.getMessage());
            req.getRequestDispatcher("/member/pay_fail.tiles").forward(req, resp);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
