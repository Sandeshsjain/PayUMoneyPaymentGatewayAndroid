package com.example.paymentgatewaymain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    String TAG = "StartPaymentActivity" , txnid = "txt12346", amount= "200",phone="9617958604",prodname = "he",firstName = "sandesh", email = "sandesh.jain.7374@gmail.com",
            merchantId = "5884494" , merchantKey = "qZtEgloC",salt = "BuvBXdsVpm",udf1="",udf10="",udf2="",udf3="",udf4="",udf5="",udf6="",udf7="",udf8="",udf9="";
    String serverCalculatedHash;
    Button button,takeImage;
    static final int REQUEST_IMAGE_CAPTURE = 10;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String hashSequence = merchantKey+"|"+txnid+"|"+amount+"|"+prodname+"|"+firstName+"|"+email+"|||||||||||"+salt;
        serverCalculatedHash = getSHA512(hashSequence);
        button = findViewById(R.id.button_payment);
        takeImage = findViewById(R.id.buttonTakePhoto);
        imageView = findViewById(R.id.imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPay(serverCalculatedHash);
            }
        });
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takepic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takepic,REQUEST_IMAGE_CAPTURE);
            }
        });

    }
    private void startPay(String serverCalculatedHash) {
        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount(amount)                          // Payment amount
                .setTxnId(txnid)                     // Transaction ID
                .setPhone(phone)                   // User Phone number
                .setProductName(prodname)                   // Product Name or description
                .setFirstName(firstName)                              // User First name
                .setEmail(email)              // User Email ID
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")     // Success URL (surl)
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php") //Failure URL (furl)
                .setUdf1(udf1)
                .setUdf2(udf2)
                .setUdf3(udf3)
                .setUdf4(udf4)
                .setUdf5(udf5)
                .setUdf6(udf6)
                .setUdf7(udf7)
                .setUdf8(udf8)
                .setUdf9(udf9)
                .setUdf10(udf10)
                .setIsDebug(true)                              // Integration environment - true (Debug)/ false(Production)
                .setKey(merchantKey)                        // Merchant key
                .setMerchantId(merchantId);
        try {
            PayUmoneySdkInitializer.PaymentParam paymentParam = builder.build();
            paymentParam.setMerchantHash(serverCalculatedHash);
            //PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam,this,R.style.AppTheme_default,true);
            PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam,this,R.style.AppTheme_default,false);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Inside exception: "+e);
        }
    }

    private String getSHA512(String input) {
        String toReturn = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input.getBytes("utf8"));
            toReturn = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra( PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE );

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                if(transactionResponse.getTransactionStatus().equals( TransactionResponse.TransactionStatus.SUCCESSFUL )){
                    //Success Transacti
                    Toast.makeText(getApplicationContext(),"Transction Success",Toast.LENGTH_SHORT).show();
                } else{
                    //Failure Transaction
                    Toast.makeText(getApplicationContext(),"Transaction Failed",Toast.LENGTH_SHORT).show();
                }

                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
            }
            else {
                Log.d(TAG, "Both objects are null!");
            }
        }
    }
}