const subscribeForm = document.getElementById('subscribe-form');
const emailInput = document.getElementById('email');
const spinner = document.querySelector('.spinner');
const subscribeResult = document.getElementById('subscribe-result');
let subscribeSuccessResetTimeoutId = 0;
const generalError = 'Something went wrong. Please try again soon.';
const subscribeResponseMessage = {
    'Subscription successful.': 'Thank you for subscribing.',
    'Failed reCAPTCHA verification.': 'If you\'re not a robot, please try again.',
    'reCAPTCHA verification failed.': generalError,
    'Server error during reCAPTCHA verification.': generalError
}

const handleSubscribeResponse = (response) => {
    clearTimeout(subscribeSuccessResetTimeoutId);

    if (response.success) {
        subscribeResult.classList.add('success');
        subscribeSuccessResetTimeoutId = setTimeout(() => {
            clearSubscribeResult();
            resetForm();
        }, 5000);
    } else {
        subscribeResult.classList.add('error');
    }

    subscribeResult.innerText = subscribeResponseMessage[response.message];
    setLoading(false);
};

const handleSubscribeError = () => {
    subscribeResult.innerText = generalError;
    subscribeResult.classList.add('error');
    setLoading(false);
}

const clearSubscribeResult = () => {
    subscribeResult.innerText = '';
    subscribeResult.className = '';
}

const resetForm = () => {
    emailInput.value = '';
}

const setLoading = (isLoading) => {
    if (isLoading) {
        spinner.classList.remove('hidden');
    } else {
        spinner.classList.add('hidden');
    }
}

subscribeForm.addEventListener('submit', function (e) {
    e.preventDefault();
    clearSubscribeResult();
    setLoading(true);

    const formData = new FormData(this);

    grecaptcha.ready(function () {
        grecaptcha.execute('YOUR_SECRET_KEY', {action: 'submit'}).then(function (token) {
            e.preventDefault();
            formData.append('recaptchaToken', token);

            fetch('/subscribe', {
                method: 'post',
                body: formData
            })
                .then(response => response.json())
                .then(response => {
                    handleSubscribeResponse(response);
                })
                .catch(handleSubscribeError);
        }).catch(handleSubscribeError);
    });
});